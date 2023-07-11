public class Kernels {

    public static float[][] sharpen = {
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };

    public static float[][] edge_detection ={
            {-1, -1, -1},
            {-1, 8, -1},
            {-1, -1, -1}
    };
    public static float[][] gaussian_blur ={
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
    };
    public static float[][] box_blur ={
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
    };
    public static float[][] emboss ={
            {-2, -1, 0},
            {-1, 1, 1},
            {0, 1, 2}
    };

    public static float[][] getKernelMatrix(String s) {
        switch (s) {
            case "sharpen":
                return sharpen;
            case "edge_detection":
                return edge_detection;
            case "gaussian_blur":
                return gaussian_blur;
            case "box_blur":
                return box_blur;
            case "emboss":
                return emboss;
            default:
                return sharpen;
        }
    }
}
