package org.andlabs.andengine.extension.physicsloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.shape.IAreaShape;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.Constants;
import org.andengine.util.SAXUtils;
import org.andengine.util.level.LevelLoader;
import org.xml.sax.Attributes;

import android.content.Context;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

/**
 * @author johannesborchardt
 */
public class PhysicsEditorLoader extends LevelLoader implements
		LoaderConstants, PhysicsConstants {

	private final String TAG = "PhysicsEditorLoader";

	// Bodies
	private List<Body> mBodies;

	private Body mBody;

	// Fixtures
	private List<FixtureDef> mFixtureDefs;

	private FixtureDef mFixtureDef;

	// Polygons
	private List<List<Shape>> mSepPolygons;

	private List<Shape> mShapes;

	private List<Vector2> mPolygon;

	// Else
	private PhysicsWorld mPhysicsWorld;

	private IAreaShape mAreaShape;

	private BodyType mBodyType;

	private boolean mUpdateRotation;

	private boolean mUpdatePosition;

	private EntityLoader mLoader;

	private Scene mScene;

	private BodyChangedListener mBodyChangedListener;

	private boolean mDebug = false;

	private VertexBufferObjectManager mVertexBufferObjectManager;

	public void reset() {
		if (mLoader != null) {
			mLoader.reset();
		}
	}

	/**
	 * Loads a XML of the PhysicsEditor (http://www.physicseditor.de) AndEngine
	 * XML format.
	 * 
	 * @param pContext
	 *            the {@link Context} of the BaseGameActivity
	 * @param pPhysicsWorld
	 *            the {@link PhysicsWorld} to which the {@link Body}s will be
	 *            attached
	 * @param pScene
	 *            the {@link Scene} on which everything will be drawn
	 * @param pAssetBasePath
	 *            the base path of the assets (e.g. "level")
	 * @param pAssetPath
	 *            the name of the level in the base path plus its path (e.g.
	 *            "level.xml")
	 * @param pShape
	 *            the {@link IAreaShape} the first body in your XML should be
	 *            attached to (e.g. an {@link AnimatedSprite})
	 * @param pUpdatePosition
	 *            whether the {@link Body}'s position should be updated
	 * @param pUpdateRotation
	 *            whether the {@link Body}'s rotation should be updated
	 * @throws IOException
	 */
	public boolean load(final Context pContext,
			final PhysicsWorld pPhysicsWorld, final String pAssetPath,
			final IAreaShape pShape, final boolean pUpdatePosition,
			final boolean pUpdateRotation) throws IOException {
		return load(pContext, pPhysicsWorld, null, pAssetPath, pShape,
				pUpdatePosition, pUpdateRotation, false, null);
	}

	/**
	 * Loads a XML of the PhysicsEditor (http://www.physicseditor.de) AndEngine
	 * XML format. Call this method if you want the polygon shapes of your XML
	 * to be drawn on your screen. Note: This will most likely cause a garbage
	 * collection!
	 * 
	 * @param pContext
	 *            the {@link Context} of the BaseGameActivity
	 * @param pPhysicsWorld
	 *            the {@link PhysicsWorld} to which the {@link Body}s will be
	 *            attached
	 * @param pScene
	 *            the {@link Scene} on which everything will be drawn
	 * @param pAssetBasePath
	 *            the base path of the assets (e.g. "level")
	 * @param pAssetPath
	 *            the name of the level in the base path plus its path (e.g.
	 *            "level.xml")
	 * @param pShape
	 *            the {@link IAreaShape} the first body in your XML should be
	 *            attached to (e.g. an {@link AnimatedSprite})
	 * @param pUpdatePosition
	 *            whether the {@link Body}'s position should be updated
	 * @param pUpdateRotation
	 *            whether the {@link Body}'s rotation should be updated
	 * @throws IOException
	 */
	public boolean loadDebug(final Context pContext,
			final PhysicsWorld pPhysicsWorld, final Scene pScene,
			final String pAssetPath, final IAreaShape pShape,
			final boolean pUpdatePosition, final boolean pUpdateRotation,
			final VertexBufferObjectManager pVertexBufferObjectManager)
			throws IOException {
		return load(pContext, pPhysicsWorld, pScene, pAssetPath, pShape,
				pUpdatePosition, pUpdateRotation, true,
				pVertexBufferObjectManager);
	}

	/**
	 * Loads a XML of the PhysicsEditor (http://www.physicseditor.de) AndEngine
	 * XML format.
	 * 
	 * @param pContext
	 *            the {@link Context} of the BaseGameActivity
	 * @param pPhysicsWorld
	 *            the {@link PhysicsWorld} to which the {@link Body}s will be
	 *            attached
	 * @param pScene
	 *            the {@link Scene} on which everything will be drawn
	 * @param pAssetBasePath
	 *            the base path of the assets (e.g. "level")
	 * @param pAssetPath
	 *            the name of the level in the base path plus it's path (e.g.
	 *            "level.xml")
	 * @param pShape
	 *            the {@link IAreaShape} the first body in your XML should be
	 *            attached to (e.g. an {@link AnimatedSprite})
	 * @param pUpdatePosition
	 *            whether the {@link Body}'s position should be updated
	 * @param pUpdateRotation
	 *            whether the {@link Body}'s rotation should be updated
	 * @param pDebug
	 *            whether the level should be debugged or not. When set to true,
	 *            the body's shapes will be drawn. Note: This will most likely
	 *            cause a garbage collection!
	 * @throws IOException
	 */
	private boolean load(final Context pContext,
			final PhysicsWorld pPhysicsWorld, final Scene pScene,
			final String pAssetPath, final IAreaShape pShape,
			final boolean pUpdatePosition, final boolean pUpdateRotation,
			final boolean pDebug, final VertexBufferObjectManager pVertexManager)
			throws IOException {

		this.mPhysicsWorld = pPhysicsWorld;
		this.mAreaShape = pShape;
		this.mUpdatePosition = pUpdatePosition;
		this.mUpdateRotation = pUpdateRotation;
		this.mScene = pScene;
		this.mDebug = pDebug;
		this.mVertexBufferObjectManager = pVertexManager;

		try {
			mLoader = new EntityLoader();

			this.registerEntityLoader(TAG_BODIES, mLoader);
			this.registerEntityLoader(TAG_BODY, mLoader);
			this.registerEntityLoader(TAG_FIXTURE, mLoader);
			this.registerEntityLoader(TAG_POLYGON, mLoader);
			this.registerEntityLoader(TAG_VERTEX, mLoader);
			this.registerEntityLoader(TAG_BODYDEF, mLoader);
			this.registerEntityLoader(TAG_METADATA, mLoader);
			this.registerEntityLoader(TAG_FORMAT, mLoader);
			this.registerEntityLoader(TAG_PTM_RATIO, mLoader);
			this.registerEntityLoader(TAG_CIRCLE, mLoader);

			loadLevelFromAsset(pContext, pAssetPath);

		} catch (RuntimeException e) {
			Log.w(TAG, "Something with the XML-Parsing went wrong", e);
			return false;
		}

		return true;
	}

	/**
	 * Set the base path to your assets. Once set, this will be the base path as
	 * long as this instance exists.
	 */
	@Override
	public void setAssetBasePath(String pAssetBasePath) {
		// Only overridden to add the comment
		super.setAssetBasePath(pAssetBasePath);
	};

	@Override
	protected void onAfterLoadLevel() {
		super.onAfterLoadLevel();

		mLoader.onAfterLoad();
	}

	private class EntityLoader implements IEntityLoader {
		private String mBodyName;
		private Vector2 mVertex;

		public EntityLoader() {
			mSepPolygons = new ArrayList<List<Shape>>();
			mShapes = new ArrayList<Shape>();
			mBodies = new ArrayList<Body>();
		}

		public void onAfterLoad() {
			addLastPoly();
			addLastPolys();
			addLastBody();
		}

		private void addLastPoly() {
			if (mShapes != null && mPolygon != null) {
				final PolygonShape polyShape = new PolygonShape();
				
				polyShape.set((Vector2[]) mPolygon.toArray(
						new Vector2[mPolygon.size()]));
				mShapes.add(polyShape);
			}
		}

		private void addLastPolys() {
			if (mSepPolygons != null && mShapes != null) {
				mSepPolygons.add(mShapes);
				mShapes = new ArrayList<Shape>();
			}
		}

		private void addLastBody() {
			if (mBodyChangedListener != null) {
				mAreaShape = mBodyChangedListener.onBodyChanged(mBodyName);
			}

			final BodyDef boxBodyDef = new BodyDef();
			boxBodyDef.type = mBodyType;

			final float[] sceneCenterCoordinates = mAreaShape.getSceneCenterCoordinates();
			boxBodyDef.position.x = sceneCenterCoordinates[Constants.VERTEX_INDEX_X] / PIXEL_TO_METER_RATIO_DEFAULT;
			boxBodyDef.position.y = sceneCenterCoordinates[Constants.VERTEX_INDEX_Y] / PIXEL_TO_METER_RATIO_DEFAULT;

			mBody = mPhysicsWorld.createBody(boxBodyDef);
		
			mBody.setUserData(mBodyName);

//			// <debugging>
//			Line line;
//			Vector2 lastVertice = null;
//			// </debugging>

			for (int e = 0; e < mSepPolygons.size() && e < mFixtureDefs.size(); e++) {
				mShapes = mSepPolygons.get(e);
				for (int i = 0; i < mShapes.size(); i++) {
					// fixture
					mFixtureDefs.get(e).shape = mSepPolygons.get(e).get(i);
					mBody.createFixture(mFixtureDefs.get(e));

					mSepPolygons.get(e).get(i).dispose();
					// <debugging>
//					if (mDebug) {
//						for (int g = 0; g < polygon.length; g++) {
//							if (lastVertice != null) {
//								// if (LOG) {
//								// Log.d(TAG, "drawing line at "
//								// + lastVertice.x + ", "
//								// + lastVertice.y + ", "
//								// + polygon[g].x + ", "
//								// + polygon[g].y);
//								// }
//								line = new Line(
//										mShape.getX()
//												+ lastVertice.x
//												* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT
//												+ mShape.getWidth() / 2,
//										mShape.getY()
//												+ lastVertice.y
//												* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT
//												+ mShape.getHeight() / 2,
//										mShape.getX()
//												+ polygon[g].x
//												* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT
//												+ mShape.getWidth() / 2,
//										mShape.getY()
//												+ polygon[g].y
//												* PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT
//												+ mShape.getHeight() / 2, 4,
//										mVertexBufferObjectManager);
//								mScene.attachChild(line);
//							}
//							lastVertice = polygon[g];
//							if (g == polygon.length - 1) {
//								lastVertice = null;
//							}
//						}
//					}
					// </debugging>

				}
			}

			mAreaShape.setZIndex(0);

			mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mAreaShape,
					mBody, mUpdatePosition, mUpdateRotation));

			mBodies.add(mBody);
		}

		@Override
		public void onLoadEntity(String pEntityName, Attributes pAttributes) {
			if (pEntityName.equals(TAG_FIXTURE)) {
				addLastPoly();
				if (mShapes.size() > 0) {
					addLastPolys();
				}

				try {
					final float density = SAXUtils.getFloatAttributeOrThrow(
							pAttributes, TAG_DENSITY);
					final float elasticity = SAXUtils.getFloatAttributeOrThrow(
							pAttributes, TAG_RESTITUTION);
					final float friction = SAXUtils.getFloatAttributeOrThrow(
							pAttributes, TAG_FRICTION);
					final boolean sensor = SAXUtils.getBooleanAttributeOrThrow(
							pAttributes, "isSensor");
					final short categoryBits = parseShort(SAXUtils
							.getAttributeOrThrow(pAttributes,
									"filter_categoryBits"));
					final short maskBits = parseShort(SAXUtils
							.getAttributeOrThrow(pAttributes, "filter_maskBits"));
					final short groupIndex = parseShort(SAXUtils
							.getAttributeOrThrow(pAttributes,
									"filter_groupIndex"));

					if (LOG) {
						Log.d(TAG, "density = " + density + ", elasticity = "
								+ elasticity + ", Friction = " + friction
								+ ", isSensor = " + sensor
								+ ", categoryBits = " + categoryBits
								+ ", maskBits = " + maskBits
								+ ", groupIndex = " + groupIndex);
					}
					mFixtureDef = PhysicsFactory.createFixtureDef(density,
							elasticity, friction, sensor, categoryBits,
							maskBits, groupIndex);
					mFixtureDefs.add(mFixtureDef);
				} catch (NumberFormatException e) {
					throw new NumberFormatException(
							"Error while parsing numbers. You probably provided too many category or mask bits in your XML.");
				}
			} else if (pEntityName.equals(TAG_POLYGON)) {
				addLastPoly();

				mPolygon = new ArrayList<Vector2>();
			} else if (pEntityName.equals(TAG_VERTEX)) {
				final float x = SAXUtils.getFloatAttributeOrThrow(pAttributes,
						"x");
				final float y = SAXUtils.getFloatAttributeOrThrow(pAttributes,
						"y");
				mVertex = new Vector2(x
						/ PIXEL_TO_METER_RATIO_DEFAULT, y
						/ PIXEL_TO_METER_RATIO_DEFAULT);

				mPolygon.add(mVertex);
			} else if (pEntityName.equals(TAG_CIRCLE)) {
				final float x = SAXUtils.getFloatAttributeOrThrow(pAttributes,
						"x") / PIXEL_TO_METER_RATIO_DEFAULT;
				final float y = SAXUtils.getFloatAttributeOrThrow(pAttributes,
						"y") / PIXEL_TO_METER_RATIO_DEFAULT;
				final float radius = SAXUtils.getFloatAttributeOrThrow(pAttributes,
						"r")  / PIXEL_TO_METER_RATIO_DEFAULT;
						
				final CircleShape circlePoly = new CircleShape();
				circlePoly.setPosition(new Vector2(x, y));
				circlePoly.setRadius(radius);
				
				mShapes.add(circlePoly);
			} else if (pEntityName.equals(TAG_BODIES)) {
				mBodies = new ArrayList<Body>();
			} else if (pEntityName.equals(TAG_BODY)) {
				if (mSepPolygons.size() > 0) {
					onAfterLoad();
				}
				mBodyName = SAXUtils.getAttributeOrThrow(pAttributes, "name");
				final boolean isDynamic = SAXUtils.getBooleanAttributeOrThrow(
						pAttributes, "dynamic");
				// TODO: Kinematic bodies are not supported by the PhysicsEditor
				// output format yet.
				if (isDynamic) {
					mBodyType = BodyType.DynamicBody;
				} else {
					mBodyType = BodyType.StaticBody;
				}
				mFixtureDefs = new ArrayList<FixtureDef>();

			} else {
				// everything else is ignored (metadata like ptm_ratio).
			}
		}

		public void reset() {
			mBodies = null;
			mBody = null;
			mFixtureDefs = null;
			mFixtureDef = null;
			mSepPolygons = null;
			mShapes = null;
			mPolygon = null;
			mPhysicsWorld = null;
			mAreaShape = null;
			mScene = null;
		}

		private short parseShort(String val) {
			Integer intVal = Integer.parseInt(val);
			return intVal.shortValue();
		}
	}

	/**
	 * @return all the bodies held by this instance
	 */
	public List<Body> getBodies() {
		return mBodies;
	}

	/**
	 * Get one specific {@link Body} by its name
	 * 
	 * @param pName
	 *            the name of the body as you specified it in the PhysicsEditor
	 * @return the {@link Body} or if nothing was found
	 */
	public Body getBody(final String pName) {
		for (final Body body : mBodies) {
			if (body.getUserData().equals(pName)) {
				return body;
			}
		}
		return null;
	}

	/**
	 * Call to register an {@link BodyChangedListener}.
	 * 
	 * @param pBodyChangedListener
	 */
	public void setBodyChangedListener(
			final BodyChangedListener pBodyChangedListener) {
		this.mBodyChangedListener = pBodyChangedListener;
	}

	/**
	 * If your XML contains more than one body, you can implement and set this
	 * interface in order to provide new ISHapes for every different body.
	 * 
	 * @author johannesborchardt
	 */
	public interface BodyChangedListener {
		/**
		 * Called when a new body was detected in the XML.
		 * 
		 * @param pBodyName
		 *            the name of the body provided in the XML.
		 * @return the shape you want this body to be attached to.
		 */
		public IAreaShape onBodyChanged(final String pBodyName);
	}
}
