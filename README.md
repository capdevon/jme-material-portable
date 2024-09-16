# jme-material-portable
This Java library extends the capabilities of jMonkeyEngine 3 by allowing you to:

* **Serialize Material objects**: Convert your jME3 materials into JSON or YAML formats.
* **Portability**: Serialized materials can be easily shared and used across different platforms.
* **Interoperability**: Integrate your jME3 materials with other tools and game engines that support JSON or YAML.

### Usage example: Creates a new Material from file
```java
// registers the Loader for the desired file format
assetManager.registerLoader(JsonMaterialLoader.class, "json");
assetManager.registerLoader(YamlMaterialLoader.class, "yaml");

// loads the Material with AssetKey
Material mat = assetManager.loadAsset(new YamlMaterialKey("Materials/MyMaterial.yaml"));
Material mat = assetManager.loadAsset(new JsonMaterialKey("Materials/MyMaterial.json"));

// loads the Material without AssetKey
Material mat = assetManager.loadMaterial("Materials/MyMaterial.yaml");
Material mat = assetManager.loadMaterial("Materials/MyMaterial.json");

// loads the Material with  MaterialDef
Material mat = new Material(assetManager, "MatDefs/New/PBRLighting.yaml");
Material mat = new Material(assetManager, "MatDefs/New/PBRLighting.json");

// loads the Material without caching the file (no need to register the loader)
YamlMaterialLoader loader = new YamlMaterialLoader();
Material mat = loader.loadMaterial(assetManager, new YamlMaterialKey("Materials/MyMaterial.yaml"));

JsonMaterialLoader loader = new JsonMaterialLoader();
Material mat = loader.loadMaterial(assetManager, new JsonMaterialKey("Materials/MyMaterial.json"));
```

### Usage example: Exports material to file in desired format
```java
public void export(Material mat, File file) {
    try {
        JsonMaterialExporter exporter = new JsonMaterialExporter();
        // YamlMaterialExporter exporter = new YamlMaterialExporter();
        exporter.save(mat, file);

    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

## Documentation
[Wiki](https://github.com/capdevon/jme-material-portable/wiki)

## Requirements
- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- java 11+

## Bug report / feature request
The best way to report bug or feature request is [github's issues page](https://github.com/capdevon/jme-material-portable/issues).
