package com.godyhm.test.bendtriangle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.GLSurfaceView.Renderer;

public class TriangleRender implements Renderer {

	private Context m_context = null;
	private final float Width = 1.2f;
	private final float Length = 1.5f;
	//book position of z axis
	private final float Pos = 0f;
	private final float cirRadius = 0.2f;
    private float rAngle = 0;
    private float incAngle = 0.75f;
	private FloatBuffer mVortexBufA = null;
	private FloatBuffer mTextBufferA = null;
	private FloatBuffer mVortexBufB = null;
	private FloatBuffer mTextBufferB = null;
	private int tIdA = 0;
	private int tIdB = 0;
	
	//list of keeping the vertexes coordinates of front-side of one page
	private ArrayList<WorldCoord> listFront = null; 
	//list of keeping the vertexes coordinates of back-side of one page
	private ArrayList<WorldCoord> listBack = null;
	private ArrayList<TextCoord> listtextcrds = null;
	private int vortexCnt = 4; 
	private int depth = 100;
	private float xAngle = 0.8f;
	private float cirAngle = 0f;
	private TriangleView m_view = null;
	public TriangleRender(Context context,TriangleView view)
	{
		m_context = context;
		m_view = view;
	}
	//basically
	/*turn page as a trace of a circle
	 * */
	private void generateCoordinates(
			float depth,//fix 
			float r,//to be fixed,the radius of circle
			//if xangle is 0,the graph will be the cylinder.I think
			float backPageoffset,//the offset between front side and back side
			float cirangle)//angle of circle arc
	{
		float tmpCirAngle = 0;
		float R = r+backPageoffset;
		float tmp = Length*Length+Width*Width;
		float sinthta = (float) (Width/Math.sqrt(tmp));
		float costhta = (float) (Length/Math.sqrt(tmp));
		if(cirangle*r>Length*sinthta)
		{
			cirAngle = 0f;
			m_view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
			return;
		}
		if(listFront.size()>0)
		{
			listFront.clear();
		}	
		if(listBack.size()>0)
		{
			listBack.clear();
		}
		if(listtextcrds.size()>0)
		{
			listtextcrds.clear();
		}
		//put top left coordinate
		listFront.add(new WorldCoord(0f,Length,Pos));
		listBack.add(new WorldCoord(0f,Length,Pos+backPageoffset));//?
		listtextcrds.add(new TextCoord(0,0));
		for(int i=0;i<=depth;i++)
		{
			tmpCirAngle = (float)(i/depth)*cirangle;
			//coordinates along the width direction
			WorldCoord xcrd = new WorldCoord();
			WorldCoord xbcrd = new WorldCoord();
			TextCoord txcrd = new TextCoord();
			//coordinates along the length direction
			WorldCoord ycrd = new WorldCoord();
			WorldCoord ybcrd = new WorldCoord();
			TextCoord tycrd = new TextCoord();
			

			//coordinate equation
			/*x=-r*tmpCirAngle*cos(xangle)+r*cos(xangle)*sin(tmpCirAngle)
			 * y=(r*cirangle-r*tmpCirAngle*cos(xangle)*cos(xangle))/sin(xangle)-r*sin(xangle)*sin(tmpCirAngle)
			 * z=r-r*cos(tmpCirAngle)
			 */
		    
			xcrd.x = (float) (-r*tmpCirAngle*costhta+
					r*costhta*Math.sin(tmpCirAngle));
			
			xcrd.y = (float) ((r*cirangle-r*tmpCirAngle*costhta*costhta)/sinthta-
					r*sinthta*Math.sin(tmpCirAngle));
			xcrd.z = -(float) (r-r*Math.cos(tmpCirAngle)+Pos);
			
			xbcrd.x = (float) (-R*tmpCirAngle*costhta+
					R*costhta*Math.sin(tmpCirAngle));
			
			xbcrd.y = (float) ((R*cirangle-R*tmpCirAngle*costhta*costhta)/sinthta-
					R*sinthta*Math.sin(tmpCirAngle));
			xbcrd.z = -(float) (R-R*Math.cos(tmpCirAngle)+Pos);
			/////////////////////////
			txcrd.u=0f;
//				txcrd.v=(Length-xcrd.y)/Length;//?
			txcrd.v=(float) ((Length-r*(cirangle-tmpCirAngle)/sinthta)/Length);
			/////////////////////////
			
			listFront.add(xcrd);
			listBack.add(xbcrd);
			listtextcrds.add(txcrd);
//			Log.i("left bottom text coordinate["+i+"]: ","u= "+txcrd.u+" v="+txcrd.v);
//			Log.i("left bottom coordinate["+i+"]: ","x= "+xcrd.x+" y="+xcrd.y+" z="+xcrd.z);
			if(i==depth)
			{
//				Log.i("Last coordinate[",+i+"]: "+"x= "+xcrd.x+" y="+xcrd.y+" z="+xcrd.z);
				return;
			}
		
			//coordinate equation
			/*x=width+(tmpCirAngle*r*sin(xangle)*sin(xangle)-cirangle*r)/cos(xangle)+r*cos(xangle)*sin(tmpCirAngle)
			 * y=r*tmpCirAngle*sin(xangle)-r*sin(xangle)*sin(tmpCirAngle)+Length
			 * z=r-r*cos(tmpCirAngle)
			 */
			ycrd.x = (float) (Width+(tmpCirAngle*r*sinthta*sinthta-
					cirangle*r)/costhta+
					r*costhta*Math.sin(tmpCirAngle));
			
			ycrd.y = (float) (r*tmpCirAngle*sinthta-
					r*Math.sin(tmpCirAngle)*sinthta)+Length;
			
			ybcrd.x = (float) (Width+(tmpCirAngle*R*sinthta*sinthta-
					cirangle*R)/costhta+
					R*costhta*Math.sin(tmpCirAngle));
			
			ybcrd.y = (float) (R*tmpCirAngle*sinthta-
					R*Math.sin(tmpCirAngle)*sinthta)+Length;
			///////////////
//				tycrd.u=ycrd.x/Width;//?
			tycrd.u =(float) ((Width-r*(cirangle-tmpCirAngle)/costhta)/Width);
			tycrd.v=0f;
			////////////////
			ycrd.z = xcrd.z;//-(float) (r-r*Math.cos(tmpCirAngle)+Pos);
			ybcrd.z=xbcrd.z;
			listFront.add(ycrd);
			listBack.add(ybcrd);
			listtextcrds.add(tycrd);
//			Log.i("left top text coordinate["+i+"]: ","u= "+tycrd.u+" v="+tycrd.v);
//			Log.i("right top coordinate["+i+"]: ","x= "+ycrd.x+" y="+ycrd.y+" z="+ycrd.z);
		}
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		generateCoordinates(depth, cirRadius,0.1f,cirAngle);
		cirAngle+=0.02f;		
		gl.glClearColor(0, 0, 0, 1.0f);	
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();
        gl.glPushMatrix();
//        gl.glTranslatef(-0.70f, 0.0f, 0f);
//        gl.glTranslatef(0.0f, -0.9f, 0f);
//        gl.glRotatef(-89, 1, 0, 0);
//        gl.glRotatef((float)(Math.atan(Length/Width)*(180/Math.PI)), 0, 0, 1);
        
        
        gl.glRotatef(40, 0, 0, 1);
        gl.glTranslatef(0.0f, -1.4f, 0f);
        
        
        
        drawTriangle(gl);      
        gl.glPopMatrix(); 
	}
	
	private void drawTriangle(GL10 gl)
	{
		WorldCoord wc1 = null;
		WorldCoord wc2 = null;
		WorldCoord wc3 = null;
		WorldCoord wc4 = null;
		TextCoord tc1 = null;
		TextCoord tc2 = null;
		TextCoord tc3 = null;
		TextCoord tc4 = null;
		int vertexCount = listFront.size();
		if(vertexCount<1)
		{
			return;
		}
		
		gl.glFrontFace(GL10.GL_CCW);
		for(int i=0;i<vertexCount-3;i+=2)
		{
			wc1 = listFront.get(i);
			wc2 = listFront.get(i+1);
			wc3 = listFront.get(i+2);
			wc4 = listFront.get(i+3);
			tc1 = listtextcrds.get(i);
			tc2 = listtextcrds.get(i+1);
			tc3 = listtextcrds.get(i+2);
			tc4 = listtextcrds.get(i+3);
			putVertextToBuf(mVortexBufA, wc1,wc2,wc3,wc4);
//			gl.glColor4f(1.0f-i*0.002f, 0.5f+i*0.002f, 0.3f+i*0.002f, 1f);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, tIdA);
			putTexureCoords(mTextBufferA, tc1,tc2,tc3,tc4);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextBufferA.position(0));
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVortexBufA.position(0));    
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0,mVortexBufA.capacity()/3);
		}
		gl.glFrontFace(GL10.GL_CW);
		for(int i=0;i<vertexCount-3;i+=2)
		{
			wc1 = listFront.get(i+3);
			wc2 = listFront.get(i+2);
			wc3 = listFront.get(i+1);
			wc4 = listFront.get(i);
			tc1 = listtextcrds.get(i+3);
			tc2 = listtextcrds.get(i+2);
			tc3 = listtextcrds.get(i+1);
			tc4 = listtextcrds.get(i);
			putVertextToBuf(mVortexBufA, wc1,wc2,wc3,wc4);
//			gl.glColor4f(1.0f-i*0.002f, 0.5f+i*0.002f, 0.3f, 1f);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, tIdB);
			putTexureCoords(mTextBufferA, tc1,tc2,tc3,tc4);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTextBufferA.position(0));
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVortexBufA.position(0));    
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0,mVortexBufA.capacity()/3);
		}
	}
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DITHER);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);//
		gl.glClearDepthf(1.0f); //
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                  GL10.GL_FASTEST); 
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_BLEND);
		
		ByteBuffer vBufA = ByteBuffer.allocateDirect(vortexCnt*3*4);
		vBufA.order(ByteOrder.nativeOrder());
		mVortexBufA = vBufA.asFloatBuffer();
		
		ByteBuffer vBufB = ByteBuffer.allocateDirect(vortexCnt*3*4);
		vBufB.order(ByteOrder.nativeOrder());
		mVortexBufB = vBufB.asFloatBuffer();
		
		ByteBuffer tbufA = ByteBuffer.allocateDirect(vortexCnt*2 * 4);
        tbufA.order(ByteOrder.nativeOrder());
        mTextBufferA = tbufA.asFloatBuffer();

		listFront = new ArrayList<WorldCoord>();
		listBack = new ArrayList<WorldCoord>();
		listtextcrds = new ArrayList<TextCoord>();
		
		initTextures(gl);
    }
    
    private void initTextures(GL10 gl)
    {
    	int [] textures = new int[2];
		gl.glGenTextures(2, textures,0);
		tIdA = textures[0];
		tIdB = textures[1];
		LoadTexture(gl, tIdA, getBitmapByPath("04.jpg"));
		LoadTexture(gl, tIdB, getBitmapByPath("03.png"));
    }
   
	private void putVertextToBuf(
			FloatBuffer fbuf,
			WorldCoord wcd1,
			WorldCoord wcd2,
			WorldCoord wcd3,
			WorldCoord wcd4)
	{
		 float[] coords = {
				 wcd1.x,wcd1.y,wcd1.z,
				 wcd2.x,wcd2.y,wcd2.z,
				 wcd3.x,wcd3.y,wcd3.z,
		    	 wcd4.x,wcd4.y,wcd4.z    		
		    };
		 
	     if(fbuf!=null)
	     {
	    	 fbuf.clear();
	     }	     
	     fbuf.put(coords);
	     fbuf.position(0);
	}
	
	private void putTexureCoords(
			FloatBuffer fbuf,
			TextCoord t1,
			TextCoord t2,
			TextCoord t3,
			TextCoord t4)
	{
		 float[] TextureArrayFront = {t1.u,t1.v,t2.u,t2.v,t3.u,t3.v,t4.u,t4.v};
	     if(fbuf!=null)
	     {
	    	 fbuf.clear();
	     }
	     fbuf.put(TextureArrayFront);
	     fbuf.position(0);      
	}
	
	private void LoadTexture(
			GL10 gl,
			int TextureId,
			Bitmap bitmap)
	{
		if(null==bitmap)
		{
			return;
		}
		gl.glBindTexture(GL10.GL_TEXTURE_2D, TextureId);     
	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);        
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D,GL10.GL_TEXTURE_MAG_FILTER,GL10.GL_LINEAR);
	    gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE,GL10.GL_REPLACE);
	    bitmap.recycle(); 
	}
    
	public Bitmap getBitmapByPath(String path)
	{
	    Bitmap bitmap = null;
	    InputStream inStream = null;
	    AssetManager aMger = m_context.getAssets();
	    try
        {
            inStream = aMger.open(path);
        }
        catch(IOException e) 
        {
            e.printStackTrace();
        }
        bitmap =  BitmapFactory.decodeStream(inStream);   
        try
        {
            if(inStream!=null)
            {
                inStream.close();
            }
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return bitmap;	    
	}
	
	public class TextCoord
	{
		public TextCoord() 
		{
			u = 0;
			v = 0;
		}
		public TextCoord(float ucrd,float vcrd) 
		{
			u = ucrd;
			v = vcrd;
		}
		public float u;
		public float v;
	}
	
	public class WorldCoord
	{
		public WorldCoord() 
		{
			x = 0;
			y = 0;
			z = 0;
		}
		public WorldCoord(float xcrd,float ycrd,float zcrd) 
		{
			x = xcrd;
			y = ycrd;
			z = zcrd;
		}
		public float x;
		public float y;
		public float z;
	}
}
