import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.grpc.BraveGrpcClientInterceptor;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import services.KeyValueServiceGrpc;
import services.KeyValueServiceGrpc.KeyValueServiceBlockingStub;
import services.UserRequest;
import services.UserServiceGrpc;
import services.UserServiceGrpc.UserServiceBlockingStub;

import java.util.Iterator;

public class ZipkinDemo {

    private static String DOCKER_IP;

    public static void main(String args[]) throws Exception {
        DOCKER_IP = System.getProperty("docker.ip");
        if (Strings.isNullOrEmpty(DOCKER_IP)) {
            System.err.println("Please set docker.ip environment variable before running");
            System.exit(-1);
        }

        ManagedChannel kvChannel = ManagedChannelBuilder.forAddress("localhost", 15001)
            .intercept(new BraveGrpcClientInterceptor(brave("kvClient")))
            .usePlaintext(true)
            .build();

        KeyValueServiceBlockingStub kvStub = KeyValueServiceGrpc.newBlockingStub(kvChannel);

        GrpcServer kvService = new GrpcServer(new KeyValueService(), 15001, brave("kv"));
        GrpcServer userService = new GrpcServer(new UserService(kvStub), 15002, brave("user"));

        kvService.start();
        userService.start();

        ManagedChannel userChannel = ManagedChannelBuilder.forAddress("localhost", 15002)
            .intercept(new BraveGrpcClientInterceptor(brave("main")))
            .usePlaintext(true)
            .build();
        UserServiceBlockingStub userStub = UserServiceGrpc.newBlockingStub(userChannel);

        int i = 0;
        System.out.println("Making 100 RPC calls");
        Iterator<String> users = Iterators.cycle("karen", "bob", "john");
        while (users.hasNext() && i < 100) {
            userStub.getUser(UserRequest.newBuilder().setName(users.next()).build());
            i++;
        }
        System.out.println("RPC calls complete");

        kvChannel.shutdown();
        userChannel.shutdown();

        kvService.stop();
        userService.stop();
    }

    private static Brave brave(String serviceName) {
        return new Brave.Builder(serviceName)
            .traceSampler(Sampler.ALWAYS_SAMPLE)
            .spanCollector(HttpSpanCollector.create(String.format("http://%s:9411", DOCKER_IP),
                new EmptySpanCollectorMetricsHandler()))
            .build();
    }

}
