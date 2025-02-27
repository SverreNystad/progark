package com.softwarearchitecture.game_client.states;

import java.util.List;
import java.util.UUID;

import com.softwarearchitecture.ecs.ECSManager;
import com.softwarearchitecture.ecs.Entity;
import com.softwarearchitecture.ecs.components.PositionComponent;
import com.softwarearchitecture.ecs.components.SpriteComponent;
import com.softwarearchitecture.ecs.components.TextComponent;
import com.softwarearchitecture.ecs.systems.InputSystem;
import com.softwarearchitecture.ecs.systems.RenderingSystem;
import com.softwarearchitecture.game_client.Controllers;
import com.softwarearchitecture.game_client.TexturePack;
import com.softwarearchitecture.game_server.GameState;
import com.softwarearchitecture.ecs.components.ButtonComponent.ButtonEnum;
import com.softwarearchitecture.math.Vector2;

public class JoinLobby extends State implements Observer, JoinGameObserver {
    private final int PAGE_Z_INDEX = 100;
    private final int TEXT_Z_INDEX = PAGE_Z_INDEX + 20;
    private final int BUTTON_Z_INDEX = PAGE_Z_INDEX + 10;
    private final float gap = 0.16f;

    public JoinLobby(Controllers defaultControllers, UUID yourId) {
        super(defaultControllers, yourId);
    }

    @Override
    protected void activate() {
        System.out.println("Join lobby activated");
        String backgroundPath = TexturePack.BACKGROUND_MULTIPLAYER;
        Entity background = new Entity();
        SpriteComponent backgroundSprite = new SpriteComponent(backgroundPath, new Vector2(1, 1));
        PositionComponent backgroundPosition = new PositionComponent(new Vector2(0, 0), PAGE_Z_INDEX);
        background.addComponent(SpriteComponent.class, backgroundSprite);
        background.addComponent(PositionComponent.class, backgroundPosition);
        ECSManager.getInstance().addLocalEntity(background);

        // Add a centered back button
        float backButtonWidth = 0.3f;
        float backButtonX = 0.5f - backButtonWidth / 2;
        ButtonFactory.createAndAddButtonEntity(ButtonEnum.BACK, new Vector2(backButtonX, 0.1f),
                new Vector2(backButtonWidth, 0.1f), this, BUTTON_Z_INDEX);

        // Add systems to the ECSManager
        RenderingSystem renderingSystem = new RenderingSystem(defaultControllers.graphicsController);
        InputSystem inputSystem = new InputSystem(defaultControllers.inputController);
        ECSManager.getInstance().addSystem(renderingSystem);
        ECSManager.getInstance().addSystem(inputSystem);

        // Create buttons for joining a game based on the available games
        List<GameState> games = defaultControllers.onlineClientMessagingController.getAllAvailableGames();
        initializeJoinLobbyTable(games);
    }

    private void initializeJoinLobbyTable(List<GameState> games) {
        // Table background
        Entity tableBackground = new Entity();
        Entity joinLobbyText = new Entity();
        SpriteComponent logoSprite = new SpriteComponent(TexturePack.BIG_EMPTY_SING, new Vector2(0.55f, 1.1f));
        SpriteComponent joinLobbyTextSprite = new SpriteComponent(TexturePack.JOIN_LOBBY, new Vector2(0.4f, 0.1f));
        PositionComponent logoPosition = new PositionComponent(new Vector2(0.228f, 0f), PAGE_Z_INDEX + 1);
        PositionComponent joinLobbyPosition = new PositionComponent(new Vector2(0.5f - 0.2f, 0.67f), TEXT_Z_INDEX + 2);
        tableBackground.addComponent(SpriteComponent.class, logoSprite);
        tableBackground.addComponent(PositionComponent.class, logoPosition);
        joinLobbyText.addComponent(SpriteComponent.class, joinLobbyTextSprite);
        joinLobbyText.addComponent(PositionComponent.class, joinLobbyPosition);
        ECSManager.getInstance().addLocalEntity(tableBackground);
        ECSManager.getInstance().addLocalEntity(joinLobbyText);
        // Table content
        float translateY = 0.48f;

        // create a fake list of placeholder games

        for (GameState game : games) {
            // Add the id and map name of as text components
            String idText = "ID: " + game.gameID.toString();
            String gameName = "MAP: " + game.mapName;
            TextComponent textComponent = new TextComponent(idText.substring(0, 12),
                    new Vector2(0.02f * 0.8f, 0.05f * 0.8f));
            TextComponent mapComponent = new TextComponent(gameName, new Vector2(0.05f * 0.8f, 0.05f * 0.8f));
            PositionComponent textPosition = new PositionComponent(new Vector2(0.32f, translateY + 0.08f),
                    TEXT_Z_INDEX);
            PositionComponent mapPosition = new PositionComponent(new Vector2(0.32f, translateY + 0.028f),
                    TEXT_Z_INDEX);
            Entity idTextEntity = new Entity();
            Entity mapTextEntity = new Entity();
            idTextEntity.addComponent(TextComponent.class, textComponent);
            idTextEntity.addComponent(PositionComponent.class, textPosition);
            mapTextEntity.addComponent(TextComponent.class, mapComponent);
            mapTextEntity.addComponent(PositionComponent.class, mapPosition);
            ECSManager.getInstance().addLocalEntity(idTextEntity);
            ECSManager.getInstance().addLocalEntity(mapTextEntity);

            // Add a join button current game
            ButtonEnum buttonType = ButtonEnum.AVAILABLE_LOBBY;
            Vector2 buttonWidth = new Vector2(0.4f, 0.16f);
            float buttonX = 0.5f - buttonWidth.x / 2;
            Vector2 position = new Vector2(buttonX, translateY);
            ButtonFactory.createAndAddButtonEntity(buttonType, position, buttonWidth, this, game, BUTTON_Z_INDEX);
            translateY -= gap;
        }
    }

    /**
     * Handles button actions based on the type of the button.
     * This state is only the intermediary menus that traverses to other states.
     * 
     * @param type: ButtonType.
     */
    @Override
    public void onAction(ButtonEnum type) {
        switch (type) {
            case BACK:
                screenManager.nextState(new Multiplayer(defaultControllers, yourId));
                break;
            default:
                break;
        }
    }

    @Override
    public void onJoinGame(GameState game) {

        // Send a message to the server to join the game
        UUID gameID = game.gameID;
        boolean didJoin = defaultControllers.onlineClientMessagingController.joinGame(gameID, yourId);
        // Wait for the server to respond
        if (didJoin) {
            screenManager.setGameId(gameID);
            screenManager.setIsLocalServer(false);
            // Change the state to the game
            screenManager.nextState(new InGame(defaultControllers, yourId, getGameName(game)));
        } else {
            // Notify the user that the game is full
            System.out.println("Game is full");
        }
    }

    private String getGameName(GameState game) {
        // TODO: Get the name of the game
        return game.mapName;
    }
}
