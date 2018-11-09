package com.clepto.blackguard;

import com.clepto.fsengine.FSEngine;
import com.clepto.fsengine.IGameLogic;

public class Main {

	public static void main(String[] args) {
		try { 
			boolean vSync = true;
			IGameLogic gameLogic = new Blackguard();
			FSEngine gameEngine = new FSEngine("Blackguard", 600, 480, vSync, gameLogic);
			gameEngine.start();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
}
