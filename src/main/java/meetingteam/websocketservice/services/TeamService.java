package meetingteam.websocketservice.services;

public interface TeamService {
    boolean isMemberOfTeam(String userId,String teamId, String channelId);
}
