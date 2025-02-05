package meetingteam.websocketservice.services;

public interface UserService {
    void changeUserStatus(String jwtToken, Boolean isOnline);
}
