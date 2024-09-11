# jme-material-portable
Material Description Format

### Usage example: Creates a new Material from file
```java
// registers the Loader for the desired file format
assetManager.registerLoader(JsonMaterialLoader.class, "json");
assetManager.registerLoader(YamlMaterialLoader.class, "yaml");

// with AssetKey
Material mat = assetManager.loadAsset(new YamlMaterialKey("MyMaterial.yaml"));
Material mat = assetManager.loadAsset(new JsonMaterialKey("MyMaterial.json"));

// with  MaterialDef
Material mat = new Material(assetManager, "MatDefs/New/PBRLighting.yaml");
Material mat = new Material(assetManager, "MatDefs/New/PBRLighting.json");
```

### Usage example: Exports material to file in desired format
```java
public void export(Material mat, File file) {
    try {
        JsonMaterialExporter exporter = new JsonMaterialExporter();
        // or
        // YamlMaterialExporter exporter = new YamlMaterialExporter();
        exporter.save(mat, file);

    } catch (IOException e) {
        e.printStackTrace();
    }
}
```
