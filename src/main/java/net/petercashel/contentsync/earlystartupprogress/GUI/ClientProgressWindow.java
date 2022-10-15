package net.petercashel.contentsync.earlystartupprogress.GUI;

import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.IUIRenderable;
import net.petercashel.contentsync.earlystartupprogress.GUI.Controls.TextLabel;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Color;
import net.petercashel.contentsync.earlystartupprogress.GUI.Core.Font;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class ClientProgressWindow {

    private final Logger logger;
    public boolean ShouldStop = false;
    // The window handle
    private long window;
    private String LastStage = "NoStage";
    private String LastMessage = "NoMessage";
    Queue<Pair<String, String>> MessageQueue = new LinkedList<>();
    private boolean Stopped = false;
    private int windowWidth;
    private int windowHeight;
    private ArrayList<IUIRenderable> Controls = new ArrayList<>();
    public TextLabel StageText;
    public TextLabel MessageText;

    public ClientProgressWindow(Logger logger) {
        this.logger = logger;
    }


    public void AddMessage(String stage, String message) {
        MessageQueue.add(new ImmutablePair<String, String>(stage, message));
    }

    public void AddRenderable(IUIRenderable e) {
        Controls.add(e);
    }
    public void RemoveRenderable(IUIRenderable e) {
        Controls.remove(e);
    }

    public void init(int width, int height, String title) {
        this.windowWidth = width;
        this.windowHeight = height;
        logger.info("ContentSync: Starting Window");

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        glfwFocusWindow(window);
        glfwFocusWindow(window);
        glfwFocusWindow(window);
        glfwFocusWindow(window);
        logger.info("ContentSync: Started Window");
    }

    public void initGL(Color clearColor) {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_PROJECTION);
        org.lwjgl.opengl.GL11.glLoadIdentity();
        org.lwjgl.opengl.GL11.glOrtho(0, windowWidth, 0, windowHeight, -1, 1);
        org.lwjgl.opengl.GL11.glMatrixMode(org.lwjgl.opengl.GL11.GL_MODELVIEW);


        // Set the clear color
        glClearColor(clearColor.getRed(), clearColor.getGreen(), clearColor.getBlue(), clearColor.getAlpha());

        glDisable(org.lwjgl.opengl.GL11.GL_DEPTH_TEST);
    }

    public int frameCount = 0;
    public void loop() {
        logger.info("ContentSync: Window Loop Start");
        try {

            // Run the rendering loop until the user has attempted to close
            // the window or has pressed the ESCAPE key.
            long start = System.currentTimeMillis();
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            long frametime = 1000 / 30;
            while ( !glfwWindowShouldClose(window) ) {
                start = System.currentTimeMillis();

                if (ShouldStop) {
                    glfwSetWindowShouldClose(window, true);
                    logger.info("ContentSync: Window Loop Stopping");
                    break;
                }

                //Inital work
                if (!MessageQueue.isEmpty() && MessageQueue.peek() != null) {
                    var message = MessageQueue.poll();
                    LastStage = message.getLeft();
                    LastMessage = message.getRight();
                }

                glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer



                // set the color of the quad (R,G,B,A)
                GL11.glColor3f(0.7f,0.7f,1.0f);
//                // draw quad
//                GL11.glBegin(GL11.GL_QUADS);
//                GL11.glVertex2f(50 + frameCount,50);
//                GL11.glVertex2f(50+100,50);
//                GL11.glVertex2f(50+100,50+100);
//                GL11.glVertex2f(50,50+100);
//                GL11.glEnd();
//
//                GL11.glBegin(GL_LINES);
//                GL11.glEnable(GL11.GL_LINE_WIDTH);
//                GL11.glLineWidth(5);
//
//                GL11.glVertex2f(50,200);
//                GL11.glVertex2f(150,200);
//
//
//                GL11.glVertex2f(250,200);
//                GL11.glVertex2f(350,200);
//
//                GL11.glEnd();

                {
                    glPushMatrix();

                    Font font = new Font(32, true);
                    String text = "ContentSync!";
                    int posx = windowWidth / 2;
                    posx = posx - (font.getWidth(text) / 2);
                    font.drawText(text, posx, windowHeight - (8 + font.getHeight(text)), Color.WHITE);

                    glPopMatrix();
                }

                //OTHER STUFF
                for (IUIRenderable renderable: Controls ) {
                    glPushMatrix();
                    renderable.render();
                    glPopMatrix();
                }

                {
                    glPushMatrix();
                    StageText.SetText(LastStage);
                    StageText.render();
                    glPopMatrix();

                    glPushMatrix();
                    MessageText.SetText(LastMessage);
                    MessageText.render();
                    glPopMatrix();
                }

                glDisable(GL_BLEND);


                glfwSwapBuffers(window); // swap the color buffers

                // Poll for window events. The key callback above will only be
                // invoked during this call.
                glfwPollEvents();

                frameCount++;
                //Target 30fps
                finish = System.currentTimeMillis();
                timeElapsed = finish - start;
                if (timeElapsed < frametime) {
                    try {
                        Thread.sleep(frametime - timeElapsed);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } catch (Exception ex) {
            logger.info("ContentSync: Window Loop Error " + ex.getMessage());
        }

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        Stopped = true;
        logger.info("ContentSync: Window Loop Stopped");
    }



    public void WaitForStop() {
        while (!Stopped) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }


}
