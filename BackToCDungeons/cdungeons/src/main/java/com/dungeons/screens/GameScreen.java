    public void returnFromCombat() {
        uiOverlayController ctrl = uiOverlaySkreen.getLoader().getController();
        ctrl.setProgress(PlayerProgress.getInstance());
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;
        stage.getScene().setRoot(gameRoot);
        player.clearInput();
        canvas.requestFocus();
        startLoop();
    }

    public void returnFromCombat() {
        uiOverlayController ctrl = uiOverlaySkreen.getLoader().getController();
        ctrl.setProgress(PlayerProgress.getInstance());
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;
        stage.getScene().setRoot(gameRoot);
        player.clearInput();
        canvas.requestFocus();
        startLoop();
        GameMusicManager.playGameplay(); 
    }


    public void returnFromCombatWithMap(String nextMapName) {
        mapManager.loadMap(nextMapName);
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;
        stage.getScene().setRoot(gameRoot);
        player.clearInput();
        canvas.requestFocus();
        startLoop();
    }

    public void returnFromCombatWithMap(String nextMapName) {
        mapManager.loadMap(nextMapName);
        mapManager.markFightDone(fightTileX, fightTileY);
        interactionLocked = false;
        stage.getScene().setRoot(gameRoot);
        player.clearInput();
        canvas.requestFocus();
        startLoop();
        GameMusicManager.playGameplay(); 
    }
