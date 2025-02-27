package com.softwarearchitecture.game_client;

import com.softwarearchitecture.ecs.GraphicsController;
import com.softwarearchitecture.ecs.InputController;
import com.softwarearchitecture.ecs.SoundController;
import com.softwarearchitecture.game_server.GameServer;
import com.softwarearchitecture.game_server.ServerMessagingController;

public class Controllers {
    public final GraphicsController graphicsController;
    public final InputController inputController;
    public final SoundController soundController;
    public final ServerMessagingController serverMessagingController;
    public final ClientMessagingController onlineClientMessagingController;
    public final ClientMessagingController localClientMessagingController;
    public final GameServer gameServer;

    public Controllers(GraphicsController graphicsController, InputController inputController, SoundController soundController, ServerMessagingController serverMessagingController, ClientMessagingController onlineClientMessagingController, ClientMessagingController localClientMessagingController, GameServer gameServer) {
        this.graphicsController = graphicsController;
        this.inputController = inputController;
        this.soundController = soundController;
        this.serverMessagingController = serverMessagingController;
        this.onlineClientMessagingController = onlineClientMessagingController;
        this.localClientMessagingController = localClientMessagingController;
        this.gameServer = gameServer;
    }
}
