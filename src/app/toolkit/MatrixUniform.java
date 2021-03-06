/*******************************************************************************
 * Copyright (c) 2013 Henrik Tramberend, Marc Latoschik.
 * All rights reserved.
 *******************************************************************************/
package app.toolkit;

import static org.lwjgl.opengl.GL20.glUniformMatrix4;
import static vecmath.vecmathimp.FactoryDefault.vecmath;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import vecmath.Matrix;


//Select the factory we want to use.

/**
 * Encapsulate the transfer of matrix values from the OpenGL application to an
 * GLSL shader uniform variable.
 */
public class MatrixUniform extends Uniform {

  private final FloatBuffer buffer;

  /**
   * Create a new named uniform binding for the specified shader program.
   * 
   * @param program
   *          The shader program that this uniform is bound to.
   * @param name
   *          The name of the uniform varibale as seen in the shader program.
   */
  public MatrixUniform(int program, String name) {
    super(program, name);
    buffer = BufferUtils.createFloatBuffer(16);
    buffer.put(vecmath.identityMatrix().asArray());
    buffer.rewind();
  }

  /**
   * Transfer a new value to the shader uniform variable.
   * 
   * @param m
   *          The new matix value for the uniform variable.
   */
  public void set(Matrix m) {
    buffer.rewind();
    buffer.put(m.asArray());
    buffer.rewind();

    glUniformMatrix4(location, false, buffer);
  }
}
