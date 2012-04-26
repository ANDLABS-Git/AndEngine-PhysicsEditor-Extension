AndEngine-PhysicsEditor-Extension
=================================

This extension provides a reader to the AndEngine Exporter format of the PhysicsEditor (http://www.physicseditor.de/). This gives you the power to draw your physical shapes and integrate them with just one line of code.

Use it like this:
1. Provide the AndEngine and AndEnginePhysicsBox2DExtension
2. Put your XML output somewhere in your assets, for example in a folder called 'xml'.
3. Use the PhysicsEditorLoader like so:
```java
final PhysicsEditorLoader loader = new PhysicsEditorLoader();
try {
    loader.load(this, // the context
        mPhysicsWorld, // an already initialized physical world
        "xml/", // the base path to your xml files
        "your_def.xml", // the AndEngine exporter XML of your body or bodies
        yourShape, // the IAreaShape the physics definition will be attached to
        true, // whether the physics definition's position should be updated or not
        true // whether the physics definition's rotation should be updated or not
        );
} catch (IOException e) {
    e.printStackTrace();
}
```
When loading multiple definitions, call
```java
loader.reset();
```
in between.
For debugging, you can also call 
```java
loader.loadDebug(pContext, pPhysicsWorld, pScene, pAssetBasePath, pAssetPath, 
     pShape, pUpdatePosition, pUpdateRotation, pVertexBufferObjectManager)
```
to see into which triangles your definition is separated.

You also might want to take a look at the examples: https://github.com/ANDLABS-Git/AndEngine-PhysicsEditor-Extension-Examples