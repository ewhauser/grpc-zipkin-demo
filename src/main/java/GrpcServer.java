import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.grpc.BraveGrpcServerInterceptor;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;

import java.io.IOException;

public class GrpcServer {

    /* The port on which the server should run */
    private int port;
    private Brave brave;
    private Server server;
    private BindableService bindableService;

    public GrpcServer(BindableService bindableService, int port, Brave brave) {
        this.bindableService = bindableService;
        this.port = port;
        this.brave = brave;
    }

    public void start() throws IOException {
      server = ServerBuilder.forPort(port)
          .addService(ServerInterceptors.intercept(bindableService, new BraveGrpcServerInterceptor(brave)))
          .build()
          .start();
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          // Use stderr here since the logger may have been reset by its JVM shutdown hook.
          System.err.println("*** shutting down gRPC server since JVM is shutting down");
          GrpcServer.this.stop();
          System.err.println("*** server shut down");
        }
      });
    }

    public void stop() {
      if (server != null) {
        server.shutdown();
      }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
      if (server != null) {
        server.awaitTermination();
      }
    }

}
