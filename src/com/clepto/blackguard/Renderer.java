package com.clepto.blackguard;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.clepto.blackguard.utils.Utils;
import com.clepto.fsengine.GameActor;
import com.clepto.fsengine.Window;
import com.clepto.fsengine.graphics.Camera;
import com.clepto.fsengine.graphics.Mesh;
import com.clepto.fsengine.graphics.ShaderProgram;
import com.clepto.fsengine.graphics.Transformation;
import com.clepto.fsengine.graphics.lighting.DirectionalLight;
import com.clepto.fsengine.graphics.lighting.PointLight;

public class Renderer {
	
	private static final float FOV = (float) Math.toRadians(60.0f);
	
	private static final float Z_NEAR = 0.01f;
	
	private static final float Z_FAR = 1000.f;
	
	private final Transformation transformation;
	
	private ShaderProgram shaderProgram;
	
	private float specularPower;
	
	public Renderer() {
		transformation = new Transformation();
		specularPower = 10f;
	}
	
	public void init(Window window) throws Exception {
		shaderProgram = new ShaderProgram();
		shaderProgram.createVertexShader(Utils.loadResource("shaders/vertex.vs"));
		shaderProgram.createFragmentShader(Utils.loadResource("shaders/fragment.fs"));
		shaderProgram.link();
		
		shaderProgram.createUniform("projectionMatrix");
		shaderProgram.createUniform("modelViewMatrix");
		shaderProgram.createUniform("texture_sampler");
		
		shaderProgram.createMaterialUniform("material");
		
		shaderProgram.createUniform("specularPower");
		shaderProgram.createUniform("ambientLight");
		shaderProgram.createPointLightUniform("pointLight");
		shaderProgram.createDirectionalLightUniform("directionalLight");
		
		glEnable(GL_DEPTH_TEST);
		window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	}
	
	public void render(Window window, Camera camera, GameActor[] gameActors, Vector3f ambientLight, PointLight pointLight, DirectionalLight directionalLight) {
		clear();
		
		if (window.isResized()) {
			glViewport(0, 0, window.getWidth(), window.getHeight());
			window.setResized(false);
		}
		
		shaderProgram.bind();
		
		Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
		shaderProgram.setUniform("projectionMatrix", projectionMatrix);
		
		Matrix4f viewMatrix = transformation.getViewMatrix(camera);
		
		shaderProgram.setUniform("ambientLight", ambientLight);
		shaderProgram.setUniform("specularPower", specularPower);
		
		PointLight currPointLight = new PointLight(pointLight);
		Vector3f lightPos = currPointLight.getPosition();
		Vector4f aux = new Vector4f(lightPos, 1);
		aux.mul(viewMatrix);
		lightPos.x = aux.x;
		lightPos.y = aux.y;
		lightPos.z = aux.z;
		shaderProgram.setUniform("pointLight", currPointLight);
		
		DirectionalLight currDirLight = new DirectionalLight(directionalLight);
		Vector4f dir = new Vector4f(currDirLight.getDirection(), 0);
		dir.mul(viewMatrix);
		currDirLight.setDirection(new Vector3f(dir.x, dir.y, dir.z));
		shaderProgram.setUniform("directionalLight", directionalLight);
		
		shaderProgram.setUniform("texture_sampler", 0);
		
		for (GameActor gameActor : gameActors) {
			Mesh mesh = gameActor.getMesh();
			Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameActor, viewMatrix);
			shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
			shaderProgram.setUniform("material", mesh.getMaterial());
			mesh.render();
		}
		
		shaderProgram.unbind();
	}
	
	public void clear() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	public void cleanup() {
		if (shaderProgram != null) {
			shaderProgram.cleanup();
		}
	}
	
}
