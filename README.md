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


To run the app

docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres

execute student.sql

Deploy the war file in tomcat or jetty.

The URL is http://localhost:8080/jdbctraining-1.0.0-BUILD-SNAPSHOT/

Click on students. This is async (but works with full ResultSet at once, so no streaming, and is blocking - simply the limitations when wrapping the JDBC).