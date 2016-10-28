Spring MVC application using JDBC operation using JDBCTemplate for CRUD operation 

In this article we show a progression of transformations taking a legacy application to a reactive one.

The reactive programming paradigm is a form of declarative programming that formulates programs as stream transformations.
It brings a higher level of abstraction into the
We start with a classic setup of a web server with a database backend.

What we hope to achieve, even for such a simple application, is firstly, to work in a functional style, which allows code to be composed, thus reused, and to be easier to grasp, given its higher level of abstraction, immutability and the fluent style.

Secondly, we could build a server that sustains more concurrent users than the number of available threads, with all the users actually having code executed for them (not enqued to a thread pool).
We will do this by having the application react when some information comes back from the database, instead of having the thread wait for the information.
This is a transformation from a pull mechanism (doing blocking I/O) to a push mechanism that continues execution when data becomes available or some event occurs, like timeout or an error.

Thirdly, a more responsive application. In extreme, the first bytes coming out of the database server could already add information on the browser view.
This is achievable with Servlet 3.1 streaming API and will become available with Spring Reactor and the associated web extensions, if we talk about the Spring stack.

We want to introduce the new paradigm in small steps, that is incremental refactoring.
We will first create more tests for the legacy application, because we rely on them for the correctness of the refactored versions of the program on its way to a more functional and reactive form.
We will then transform the signatures of interfaces between layers, from their current shape to returning an Observable, which is RxJava notion of a stream.
You may find intuitive for a List to become a stream, but not for a value. However, a Future of a single value is a particular case of a stream of future values which happens to contain a single value, or to contain zero values and an error, like a timeout, connection broken or retries exhausted.
This step does not change the nature of execution. It is still synchronous, doing blocking I/O, like JDBC does. But it is already composable. Let's explore what that means.
Let's assume we had a method getStudents which used to return a List<Student> and now returns an Observable<Student>.
We have on this observable all the methods we have on the Java 8 Stream, and some more.
So we could map, filter, flatMap and even zipWith, which is not available in Java 8.
And we could have one more thing, called pipe-lining, or constant memory processing, but not in this version of the code.
Pipelining means that I can have a transformation pipe from the database to the browser that does not need to accumulate all the results in memory, but only a constant amount of data, to hold a batch of rows that is currently processed.

What we can also already have is composition. Suppose we have 2 backends, one database, where we do OLTP and we keep aggregated OLAP data, and HBase, where we have all the current and historical data.
We could have a fallback mechanism, that when the database is down, queries HBase and aggregates the data from there, instead of getting the already aggregated data from Postgres.
Further, each backend can be retried a number of times, timeouts can be configured, and even queried in parallel and whichever responds faster gets delivered.
And all, via stream transformations provided by RxJava. Composition can also mean multiple steps of computation, either synchronous (like string sanitising) or asynchronous (like doing non blocking I/O).
We will compare doing a retry mechanism by hand versus doing it in RxJava. With an exponential backoff. And with batching, that is the retry should be done for multiple requests, queries in this case, at once, for efficiency.
It is true that in case of JDBC, some retry at the connection level is being done in connection pools.
But composing data from multiple sources depending on availability and speed, graceful degradation, retry policies, are all hard problems to get right, efficient in both latency and throughput, and simple.
Having a reactive framework like RxJava guide your processing pipelines can mean orders of magnitude differences in code simplicity and performance.

Finally, assume we have a multi-step processing pipeline. Switching one step from a synchronous IP to geo call in a local cache to calling a web service which would potentially travel the network,
will remain just an implementation detail fo the method with type Function<IP,Observable<Geo>>. Nothing outside it needs to change or become aware of the asynchronous nature of the computation, the source of concurrency i.e. on which thread will the response be processed.


Going back to the refactoring steps, the first one, to change signatures from
 "List<Student> getStudents()",
 which is an interface functionally identical (isomorphical to) to Supplier<List<Student>>,
 to "Observable<Student> getStudents()" i.e. Supplier<Observable<Student>>,
 and "Student getStudent"
 which is an interface functionally identical (isomorphical to) to Supplier<Student>,
 to "Observable<Student> getStudents()" i.e. Supplier<Observable<Student>>,
 or to "Single<Student> getStudent()" i.e. Supplier<Single<Student>>,
 is just meant to set the contracts (interfaces) between layers/components to reactive,
 so that each one can evolve independently, in parallel teams, and not affecting the others.

So a way of understanding this, is that inserting reactive paradigm can be done top-down, by wrapping big parts into a reactive envelope (API),
and making the big parts interact reactively (in API, not in implementation), and then break down inside each boulder where development is more intense, or complexity higher, or more need for scalability exists.

The second step, after simply converting return studentList to return Observable.of(studentList) is to use a reactive library to access the backend.
Some databases like HBase have async clients which are non blocking (in the sense that no single I/O operation is blocking any thread, but some dedicated threads can poll on the status of hundreds of thousands of ongoing flows).
Since JDBC is blocking by nature (since it returns an already populated ResultSet), even though there are libraries that provide an RxJava envelope to JDBC,
for instance,  the JDBC will still block, but in potentially a different thread, as configured in the Rx Observable.
So using an RxJava envelope of JDBC itself, allows for asincronicity in the main flow, but other threads will still be used to block on waiting for full answer from the database.
This of course limits scalability to the same degree as the plain JDBC does, but you could plug this into a non blocking server like Vert.x (which is a Java equivalent for say NodeJS).

The third step is to finally introduce a true reactive library. There are a few, even for relational databases, but you can find more when focusing on a specific database, such as Postgres.
This is because the database access library is specific for each low level protocol of each database.
Here we use postgres-async-driver project, which is using RxJava as an API of its library.

Similarily to steps taken on the backend, the frontend meaning controller part also needs transforming, since classical Servlet and Spring MVC are blocking.
However Servlet 3.0 and Servlet 3.1 take steps to async computation and even streaming.
Spring covers those features in Spring MVC Async, which we already use in the project, namely the DeferredResult.
Spring plans support for streaming in upcoming Spring Reactive Web, powered by Spring Reactor ecosystem.

So given we have a studentDAO.getAllStudents() of type Observable<Student>, here is how we transform it and in the end we subscribe to it in a way
that allows us to chain the result back into the DeferredResult, the async object Spring MVC provides.

    @RequestMapping(value = "/student.html", method = RequestMethod.GET)
    public DeferredResult<ModelAndView> show_users(Model model) {
        return deferredStudentsView("home");
    }

    private DeferredResult<ModelAndView> deferredStudentsView(String view) {
        return toDeferredResult(studentDAO.getAllStudents()
                .toList()
                .map(students -> {
                    ModelAndView modelAndView = new ModelAndView(view);
                    modelAndView.addObject("students", students);
                    return modelAndView;
                }));
    }
    public static  <T> DeferredResult<T> toDeferredResult(Observable<T> observable) {
        DeferredResult<T> deferredResult = new DeferredResult<>();
        observable.subscribe(result -> deferredResult.setResult(result), e -> deferredResult.setErrorResult(e));
        return deferredResult;
    }

The next step is to stream the students json to the browser, creating the pipe from database to browser. In micro services, it could be a pipe through a chain od nodes.
We can introduce Vert.x, a web server in Java which supports streaming, and has compatibility with RxJava even.
Note that now we step out of Servlet world. There are also classic containers like Jetty which have direct API, not just servlet.

Another next step is update operations, and even transactions.

Here is how a transaction would look like:

private Observable<Long> dml(String query, Object... params) {
        return db.begin().flatMap(transaction ->
                transaction.querySet(query, params)
                        .flatMap(resultSet -> transaction.commit().map(__ -> resultSet.iterator().next().getLong(0)))
                        .doOnError(e-> transaction.rollback())
        );
    }

This is a single-statement transaction, but it illustrates how you can do transactions in an async reactive API.
Both transaction begin, and commit, or rollback, are monadic functions: they return an Observable and they can be chained with flatMap.
Let's follow the example above. first, the signature. The dml (data modification language statement) execution function, takes a DML statement (like an UPDATE, or INSERT),
and their parameters, if any, and "schedules" it for execution.
First we notice that db.begin returns Observable<Transaction>. The transaction is not created right away, because it involves I/O with the database.
So this is an asynchronous operation, that when completed returns a transaction object on which commit or rollback can be called at the right time.
This transaction object will be passed from java closure to java closure, as we see above: first, transaction is available as an argument to flatmap.
 There it is used in two spots: first to launch the DML statement itself within the transaction.
 But then, the result of querySet which executes the DML, is also an Observable.
 This Observable, holding the result of the DML (like, a Row with updated rows count), is further transformed with flatMap,
 to another observable. It is there, in this second flatMap, that transaction object is once again used to commit the transaction.
 There, the transaction variable is closed over by the lambda function given as argument to this second flatMap.
 This is the manner in which you can send data from one part of an async flow to the other, since we do not have a thread, but we still have lexical scope.
If successful, the transaction commit result will be transformed back to the update count, which can be used by callers.
Here we cannot close over the result count, since the called is not in the same lexical scope with us here, so we need to encapsulate the result as the data type of the resulting observable.
If we need to carry multiple results, tuples are available in some languages, but in Java we have them in libraries.
On error, rollback is called. The error bubbles up further. It could be swallowed as well, but we choose not to, here.
This is the second closure where transaction variable is used. The variable has to be effectively immutable to do so.

This manner of capturing results can result in a very nested expression, a closure in closure in closure, transformation in transformation in yet another one.
Languages like Scala and Haskell have library or language constructs to flatten this. The "do" construct in Haskell, or the for comprehension in Scala aim to allow such a chaining go smooth and flat.

 In Haskell it would look like this:
 do
    transaction <- db.begin
    resultSet <- transaction.querySet(query)
    commitSignal <- transaction.commit
    return resultSet.getcount

 You can see that any variable created along the way is accesible in the lexical scope, but the structure remains flat.
 I do not know an equivalent in Java, but I am searching for it.

In reactive world we aim to bring a blocking application to a non-blocking state.
A blocking application is one that when doing I/O, like opening a TCP connection, blocks the thread.
Most of the initial Java APIs for opening sockets, talking to databases (JDBC), file/inputStream/outputStream, all were blocking APIs.
The same about initial versions of Servlet API, and most things in Java were like that.
Certain things started to change, like Servlet 3.x integrated a few concepts like async and streaming.
But in a typical J2EE application, one would typically find blocking calls, which is not always a bad thing.
Blocking semantics is easier to understand than explicit async API.
Some languages like C#, Scala and Haskell have constructs that transparently generate non blocking implementations from a blocking code.
Such as the async high order function in C# and Scala.
With Java, to my knowledge, the most advanced way to do things non blocking is with Reactive Streams or RxJava 1.0 (since RxJava 2.0 is already implementing Reactive Streams)
and with non blocking libraries such as Netty.
However things remain pretty explicit, so the entry barrier can be higher.
Still, when you need to support more concurrent users than the number of threads, or when you want to minimize costs and your application is I/O-bound,
then doing things non blocking will get your extra order of magnitude in scalability and elasticity and in cost reduction.
An application is I/O bound if doing I/O is the bottleneck, and CPU-bound if that is the bottleneck.
When discussing elasticity or robustness, the most impactful way to think about it is the moment where all your threads are waiting for I/O.
A modern JVM can withstand 5000 threads let's say. This means that when 5000 calls to various web services are waiting on the respective threads, in a blocking application,
simply no more user requests can be executed at that stage (they can only be enqueued for later processing by some specialized threads doing just enqueing).
That can be fine in a controlled context, such as a corporate intranet, but is for sure not what a startup needs when suddenly 10 times more users check out their product.
Of course one complementary solution to traffic spikes is horizontal scalability, bringing up more servers, but that is not elastic enough, not to mention costs.
Again, it all depends on the kind of I/O an application does. But even if the HTTP is the only potentially-slow I/O an internet service is exposed to,
and all the other I/O ops are with internal databases and services which are HA (highly available) and low latency, then at least HTTP will move bytes slowly with a slow client on the other side of the planet.
It is true that this problem is taken care by professional load balancers.
But the other thing is you never know when the most "highly available" internal or external service goes down, and when the most "low latency" service is actually "near-realtime" not hard-realtime, and at that moment it will just respond slow because of garbage collecting.
Then if you are blocking in only parts of your stack, there will be a blocking bubble:
which means that threads will start blocking on the slowest blocking I/O and bring resources to a halt because of a single slow blocking access which is requested by 5% of the traffic and has low business importance.

Now that I convinced you that making an application non blocking is worth it in some situations,
lets' look at our legacy application.
It is blocking in all its layers, in HTTP, in database access, so let's start from this.
Unless all the layers on a vertical (here HTTP and database access) are being made async, the full flow cannot be async.
There is also a difference between async and non blocking in that while non blocking implies async (unless we have language constructs),
async can be always done for blocking call by simply "moving" it to a different thread.
This has some of the same issues as the initial blocking solution but can be a step towards the end goal, in an incremental approach.
For the HTTP side, we are already half-covered by current state of Servlet and Spring MVC.
Meaning we have async behavior, but not streaming.
What does it mean async behavior? It means that when the database has finished responding, the processing kicks in.
When processing is finished, the web layer kicks in, rendering stuff.
When the web page is rendered (or the JSON payload), the HTTP layer is call with "here, your full response payload".
The next step would be streaming: when the database tells processing layer "here, some more data for you", processing layer takes it.
This "takes it" does not imply dedicated thread, if using NIO ways, or, say in Linux, epoll.
Here the idea is that 100K connections are being queried by a single thread to the OS with the questions "anything new on the 100K connections?".
Then the processing layer can do a transformation that outputs more semantic units, like "students".
It may be useful sometimes, if the data from the database represents just part of the student, to keep the partial info in the processing layer buffers.
When a data bulk from the db finally has all the data on that student, it can be "closed" and sent to the upper layer, rendering.
In such a pipeline, any component can stream at any granularity: some will just copy bytes from left to right, while others will send full student instances or even batches of them,
while others, like the DefferableResult of MVC Spring Async, will need to whole result before starting to write an HTTP response.

If a single component in your pipeline is waiting for the whole result, it is best to gather all your data in the layer which consumes the smallest amount of memory, if that is your concern.
Otherwise, let it flow to the first wait-all component. That will naturally "block" the whole stream until it processed all of it and pass it up when a new stream happens.
The advantage of streaming all the way (when all the components, from HTTP to database, in our simple example, are streaming to the level/granularity say of sayy batches of 1000 students, not more),
then the application is more elastic since it consumes a limited amount of memory per request, and even a constant amount of it!



To run the app

docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres

execute student.sql

Deploy the war file in tomcat or jetty.

The URL is http://localhost:8080/jdbctraining-1.0.0-BUILD-SNAPSHOT/

Click on students. This is async (but works with full ResultSet at once, so no streaming, and is blocking - simply the limitations when wrapping the JDBC).