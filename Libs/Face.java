package Libs;

// Face class for 3D geometry
public class Face {
    public int[] vertexIndices;
    
    public Face(int... indices) {
        this.vertexIndices = indices;
    }
}
