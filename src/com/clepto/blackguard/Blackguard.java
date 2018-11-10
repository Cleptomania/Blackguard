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
import com.clepto.fsengine.graphics.Texture;
import com.clepto.fsengine.graphics.lighting.DirectionalLight;
import com.clepto.fsengine.graphics.lighting.PointLight;
import com.clepto.fsengine.graphics.lighting.SpotLight;

public class Blackguard implements IGameLogic {
	
	private final Vector3f cameraInp;
	
	private final Renderer renderer;
	
	private final Camera camera;
	
	private GameActor[] gameActors;
	
	private Vector3f ambientLight;
	
	private PointLight[] pointLights;
	
	private SpotLight[] spotLights;
	
	private DirectionalLight directionalLight;
	
	private float dirLightAngle;
	
	private float spotAngle = 0;
	
	private float spotInc = 1;
	
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
		PointLight pointLight = new PointLight(lightColor, lightPosition, lightIntensity);
		PointLight.Attenuation att = new PointLight.Attenuation(0.0f, 0.0f, 1.0f);
		pointLight.setAttenuation(att);
		pointLights = new PointLight[] { pointLight };
		
		lightPosition = new Vector3f(0.0f, 0.0f, 10.0f);
		pointLight = new PointLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
		att = new PointLight.Attenuation(0.0f, 0.0f, 0.02f);
		pointLight.setAttenuation(att);
		Vector3f coneDir = new Vector3f(0, 0, -1);
		float cutoff = (float) Math.cos(Math.toRadians(140));
		SpotLight spotLight = new SpotLight(pointLight, coneDir, cutoff);
		spotLights = new SpotLight[] { spotLight, new SpotLight(spotLight) };
		
		lightPosition = new Vector3f(-1.0f, 0.0f, 0.0f);
		lightColor = new Vector3f(1.0f, 1.0f, 1.0f);
		directionalLight = new DirectionalLight(lightColor, lightPosition, lightIntensity);
		
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
		
		float lightPos = pointLights[0].getPosition().z;
		if (window.isKeyPressed(GLFW_KEY_Z)) {
			this.pointLights[0].getPosition().z = lightPos + 0.025f;
		} else if (window.isKeyPressed(GLFW_KEY_C)) {
			this.pointLights[0].getPosition().z = lightPos - 0.025f;
		}
	}

	@Override
	public void update(float interval, MouseInput mouseInput) {
		camera.movePosition(cameraInp.x * CAMERA_INPUT_STEP, cameraInp.y * CAMERA_INPUT_STEP, cameraInp.z * CAMERA_INPUT_STEP);
		
		if (mouseInput.isRightButtonPressed()) {
			Vector2f rotVec = mouseInput.getDisplVec();
			camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
		}
		
		spotAngle += spotInc * 0.05f;
		if (spotAngle > 2) {
			spotInc = -1;
		} else if (spotAngle < -2) {
			spotInc = 1;
		}
		double spotAngleRad = Math.toRadians(spotAngle);
		Vector3f coneDir = spotLights[0].getConeDirection();
		coneDir.y = (float) Math.sin(spotAngleRad);
		
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
		renderer.render(window, camera, gameActors, ambientLight, pointLights, spotLights, directionalLight);
	}
	
	@Override
	public void cleanup() {
		renderer.cleanup();
		for (GameActor gameActor : gameActors) {
			gameActor.getMesh().cleanUp();
		}
	}
	
	
	
}