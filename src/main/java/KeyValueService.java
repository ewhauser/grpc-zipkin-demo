import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import io.grpc.stub.StreamObserver;

import services.GetRequest;
import services.GetResponse;
import services.KeyValueServiceGrpc.KeyValueServiceImplBase;
import services.PutRequest;
import services.PutResponse;

import java.util.Map;
import java.util.Random;

public class KeyValueService extends KeyValueServiceImplBase {

    private static Map<String, String> store = Maps.newHashMap(ImmutableMap.<String, String>builder()
        .put("bob.emailAddress", "bob@yahoo.ca")
        .put("bob.country", "Canada")
        .put("bob.active", "true")
        .put("karen.emailAddress", "karen@yahoo.com")
        .put("karen.country", "US")
        .put("karen.active", "true")
        .put("john.emailAddress", "john@yahoo.fr")
        .put("john.country", "France")
        .put("john.active", "false")
        .build()
    );

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
            store.put(request.getKey(), request.getValue());
        responseObserver.onNext(PutResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
        //Simulate some latency
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(100));
        } catch (InterruptedException ignored) {
        }

        GetResponse.Builder response = GetResponse.newBuilder();
        String value = store.get(request.getKey());
        if (value != null) {
            response.setValue(value);
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
