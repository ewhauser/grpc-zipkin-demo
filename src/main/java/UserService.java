import com.google.common.base.Preconditions;

import io.grpc.stub.StreamObserver;

import services.GetRequest;
import services.KeyValueServiceGrpc.KeyValueServiceBlockingStub;
import services.UserRequest;
import services.UserResponse;
import services.UserServiceGrpc.UserServiceImplBase;

public class UserService extends UserServiceImplBase {

    private KeyValueServiceBlockingStub keyValue;

    public UserService(KeyValueServiceBlockingStub keyValue) {
        this.keyValue = Preconditions.checkNotNull(keyValue);
    }

    @Override
    public void getUser(UserRequest request, StreamObserver<UserResponse> responseObserver) {
        UserResponse.Builder response = UserResponse.newBuilder();

        response.setName(request.getName());

        response.setEmailAddress(keyValue.get(GetRequest.newBuilder()
            .setKey(request.getName() + ".emailAddress")
            .build())
            .getValue());

        response.setCountry(keyValue.get(GetRequest.newBuilder()
            .setKey(request.getName() + ".country")
            .build())
            .getValue());

        response.setActive(Boolean.valueOf(keyValue.get(GetRequest.newBuilder()
            .setKey(request.getName() + ".active")
            .build())
            .getValue()));

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

}
