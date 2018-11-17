package com.clepto.blackguard;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.clepto.fsengine.IApplication;
import com.clepto.fsengine.MouseInput;
import com.clepto.fsengine.Window;
import com.clepto.fsengine.graphics.Camera;
import com.clepto.fsengine.graphics.Material;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.OBJLoader;
import com.clepto.fsengine.graphics.Renderer;
import com.clepto.fsengine.graphics.Texture;
import com.clepto.fsengine.scene.Scene;
import com.clepto.fsengine.scene.SceneLight;
import com.clepto.fsengine.scene.actors.Actor;

public class Blackguard implements IApplication {
	
	private final Vector3f cameraInp;
	
	private final Renderer renderer;
	
	private final Camera camera;
	
	private Scene scene;
	
	private static final float CAMERA_INPUT_STEP = 0.05f;
	
	private static final float MOUSE_SENSITIVITY = 0.2f;
	
	public Blackguard() {
		renderer = new Renderer();
		camera = new Camera();
		cameraInp = new Vector3f(0, 0, 0);
	}
	
	@Override
	public void init(Window window) throws Exception {
		renderer.init(window);
		
		scene = new Scene();		
		
		float reflectance = 1f;
		Mesh mesh = OBJLoader.loadMesh("models/cube.obj");
		Texture texture = new Texture("textures/grassblock.png");
		Material material = new Material(texture, reflectance);
		mesh.setMaterial(material);
		
		float blockScale = 0.5f;
		float skyboxScale = 10.0f;
		float extension = 2.0f;
		
		float startx = extension * (-skyboxScale + blockScale);
		float startz = extension * (skyboxScale - blockScale);
		float starty = -1.0f;
		float inc = blockScale * 2;
		
		float posx = startx;
		float posz = startz;
		float incy = 0.0f;
		int NUM_ROWS = (int)(extension * skyboxScale * 2 / inc);
		int NUM_COLS = (int)(extension * skyboxScale * 2 / inc);
		
		Actor[] actors = new Actor[NUM_ROWS * NUM_COLS];
		for (int i = 0; i < NUM_ROWS; i++) {
			for (int j = 0; j < NUM_COLS; j++) {
				Actor actor = new Actor(mesh);
				actor.setScale(blockScale);
				incy = Math.random() > 0.9f ? blockScale * 2 : 0f;
				actor.setPosition(posx, starty + incy, posz);
				actors[i*NUM_COLS + j] = actor;
				
				posx += inc;
			}
			posx = startx;
			posz -= inc;
		}
		scene.setActors(actors);
		
		setupLights();
		
		camera.getPosition().y = 5;
		
		window.setClearColor(0.1f, 0.5f, 0.8f, 1.0f);
	}
	
	private void setupLights() {
		SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);
		
		//Ambient Light
		sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
	}

	@Override
	public void input(Window window, MouseInput mouseInput) {
		cameraInp.set(0, 0, 0);
		if (window.isKeyPressed(GLFW_KEY_W)) {
			cameraInp.z = -1;
		} else if (window.isKeyPressed(GLFW_KEY_S)) {
			cameraInp.z = 1;
		}
		
		if (window.isKeyPressed(GLFW_KEY_A)) {
			cameraInp.x = 1;
		} else if (window.isKeyPressed(GLFW_KEY_D)) {
			cameraInp.x = -1;
		}
		
		if (window.isKeyPressed(GLFW_KEY_Q)) {
			cameraInp.y = -1;
		} else if (window.isKeyPressed(GLFW_KEY_E)) {
			cameraInp.y = 1;
		}
	}

	@Override
	public void update(float interval, MouseInput mouseInput) {
		camera.movePosition(cameraInp.x * CAMERA_INPUT_STEP, cameraInp.y * CAMERA_INPUT_STEP, cameraInp.z * CAMERA_INPUT_STEP);
		
		if (mouseInput.isRightButtonPressed()) {
			Vector2f rotVec = mouseInput.getDisplVec();
			camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
		}
	}

	@Override
	public void render(Window window) {
		renderer.render(window, camera, scene);
	}
	
	@Override
	public void cleanup() {
		renderer.cleanup();
		for (Actor actor : scene.getActors()) {
			actor.getMesh().cleanUp();
		}
	}
	
	
	
}