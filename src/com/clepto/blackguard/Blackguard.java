package com.clepto.blackguard;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.clepto.fsengine.IGameLogic;
import com.clepto.fsengine.MouseInput;
import com.clepto.fsengine.Window;
import com.clepto.fsengine.graphics.Camera;
import com.clepto.fsengine.graphics.Material;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.OBJLoader;
import com.clepto.fsengine.graphics.Renderer;
import com.clepto.fsengine.graphics.Texture;
import com.clepto.fsengine.graphics.lighting.DirectionalLight;
import com.clepto.fsengine.graphics.lighting.PointLight;
import com.clepto.fsengine.graphics.lighting.SpotLight;
import com.clepto.fsengine.scene.Scene;
import com.clepto.fsengine.scene.SceneLight;
import com.clepto.fsengine.scene.actors.Actor;
import com.clepto.fsengine.scene.actors.SkyBox;

public class Blackguard implements IGameLogic {
	
	private final Vector3f cameraInp;
	
	private final Renderer renderer;
	
	private final Camera camera;
	
	private Scene scene;
	
	private float dirLightAngle;
	
	private static final float CAMERA_INPUT_STEP = 0.05f;
	
	private static final float MOUSE_SENSITIVITY = 0.2f;
	
	public Blackguard() {
		renderer = new Renderer();
		camera = new Camera();
		cameraInp = new Vector3f(0, 0, 0);
		dirLightAngle = -90;
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
		
		SkyBox skybox = new SkyBox("models/skybox.obj", "textures/skybox.png");
		skybox.setScale(skyboxScale);
		scene.setSkyBox(skybox);
		
		setupLights();
		
		camera.getPosition().y = 5;
	}
	
	private void setupLights() {
		SceneLight sceneLight = new SceneLight();
		scene.setSceneLight(sceneLight);
		
		//Ambient Light
		sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
		
		//Directional Light
		float lightIntensity = 1.0f;
		Vector3f lightDirection = new Vector3f(-1, 0, 0);
		sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity));
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

		SceneLight sceneLight = scene.getSceneLight();
		
		DirectionalLight directionalLight = sceneLight.getDirectionalLight();
		dirLightAngle += 1.1f;
		if (dirLightAngle > 90) {
			directionalLight.setIntensity(0);
			if (dirLightAngle >= 360) {
				dirLightAngle = -90;
			}
		} else if (dirLightAngle <= -80 || dirLightAngle >= 80) {
			float factor = 1 - (float) (Math.abs(dirLightAngle) - 80) / 10.0f;
			directionalLight.setIntensity(factor);
			directionalLight.getColor().x = Math.max(factor, 0.9f);
			directionalLight.getColor().z = Math.max(factor, 0.5f);
		} else {
			directionalLight.setIntensity(1);
			directionalLight.getColor().x = 1;
			directionalLight.getColor().y = 1;
			directionalLight.getColor().z = 1;
		}
		double angRad = Math.toRadians(dirLightAngle);
		directionalLight.getDirection().x = (float) Math.sin(angRad);
		directionalLight.getDirection().y = (float) Math.cos(angRad);
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