#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};

struct PointLight
{
    vec3 color;
    // Light position is assumed to be in view coordinates
    vec3 position;
    float intensity;
    Attenuation att;
};

struct SpotLight
{
    PointLight pl;
    vec3 conedir;
    float cutoff;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_POINT_LIGHTS];
uniform DirectionalLight directionalLight;

vec4 ambientC;
vec4 diffuseC;
vec4 speculrC;

void setupColors(Material material, vec2 texCoord)
{
    if (material.hasTexture == 1)
    {
        ambientC = texture(texture_sampler, texCoord);
        diffuseC = ambientC;
        speculrC = ambientC;
    }
    else
    {
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        speculrC = material.specular;
    }
}

vec4 calcLightColor(vec3 lightColor, float lightIntensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specColor = vec4(0, 0, 0, 0);

    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuseC * vec4(lightColor, 1.0) * lightIntensity * diffuseFactor;

    vec3 cameraDirection = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    float specularFactor = max(dot(cameraDirection, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColor = speculrC * lightIntensity * specularFactor * material.reflectance * vec4(lightColor, 1.0);

    return (diffuseColor + specColor);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 lightDirection = light.position - position;
    vec3 to_light_dir = normalize(lightDirection);
    vec4 lightColor = calcLightColor(light.color, light.intensity, position, to_light_dir, normal);

    // Attenuation
    float distance = length(lightDirection);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return lightColor / attenuationInv;
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal)
{
    vec3 lightDirection = light.pl.position - position;
    vec3 to_light_dir = normalize(lightDirection);
    vec3 from_light_dir = -to_light_dir;
    float spotAlpha = dot(from_light_dir, normalize(light.conedir));

    vec4 color = vec4(0, 0, 0, 0);

    if (spotAlpha > light.cutoff)
    {
        color = calcPointLight(light.pl, position, normal);
        color *= (1.0 - (1.0 - spotAlpha)/(1.0 - light.cutoff));
    }

    return color;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 normal)
{
    return calcLightColor(light.color, light.intensity, position, normalize(light.direction), normal);
}

void main()
{
    setupColors(material, outTexCoord);

    vec4 diffuseSpecularComp = calcDirectionalLight(directionalLight, mvVertexPos, mvVertexNormal);
    
    for (int i=0; i <MAX_POINT_LIGHTS; i++)
    {
        if (pointLights[i].intensity > 0)
        {
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, mvVertexNormal);
        }
    }

    for (int i=0; i<MAX_SPOT_LIGHTS; i++)
    {
        diffuseSpecularComp += calcSpotLight(spotLights[i], mvVertexPos, mvVertexNormal);
    }

    fragColor = ambientC * vec4(ambientLight, 1.0) + diffuseSpecularComp;
}