package com.clepto.blackguard;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;
import org.joml.Vector3f;

import com.clepto.fsengine.GameActor;
import com.clepto.fsengine.IGameLogic;
import com.clepto.fsengine.MouseInput;
import com.clepto.fsengine.Window;
import com.clepto.fsengine.graphics.Camera;
import com.clepto.fsengine.graphics.Material;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.OBJLoader;
import com.clepto.fsengine.graphics.PointLight;
import com.clepto.fsengine.graphics.Texture;

public class Blackguard implements IGameLogic {
	
	private final Vector3f cameraInp;
	
	private final Renderer renderer;
	
	private final Camera camera;
	
	private GameActor[] gameActors;
	
	private Vector3f ambientLight;
	
	private PointLight pointLight;
	
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
		
		float reflectance = 1f;
		Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
		Texture texture = new Texture("/textures/grassblock.png");
		Material material = new Material(texture, reflectance);
		
		mesh.setMaterial(material);
		GameActor gameActor = new GameActor(mesh);
		gameActor.setScale(0.5f);
		gameActor.setPosition(0,  0, -2);
		gameActors = new GameActor[] { gameActor };
		
		ambientLight = new Vector3f(0.3f, 0.3f, 0.3f);
		
		Vector3f lightColor = new Vector3f(1.0f, 0.0f, 0.0f);
		Vector3f lightPosition = new Vector3f(0.0f, 0.0f, 1.0f);
		float lightIntensity = 1.0f;
		pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
		PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
		pointLight.setAttenuation(att);
		
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
		
		float lightPos = pointLight.getPosition().z;
		if (window.isKeyPressed(GLFW_KEY_Z)) {
			this.pointLight.getPosition().z = lightPos + 0.1f;
		} else if (window.isKeyPressed(GLFW_KEY_C)) {
			this.pointLight.getPosition().z = lightPos - 0.1f;
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
		renderer.render(window, camera, gameActors, ambientLight, pointLight);
	}
	
	@Override
	public void cleanup() {
		renderer.cleanup();
		for (GameActor gameActor : gameActors) {
			gameActor.getMesh().cleanUp();
		}
	}
	
	
	
}