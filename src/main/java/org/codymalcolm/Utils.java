package org.codymalcolm;

import java.io.File;

public class Utils {
    public static String getFilename(String filename) {
        File temp = new File(filename);
        int i = 1;
        while (temp.exists()) {
            if (i == 1) {
                filename = incrementIteration(filename);
            } else {
                filename = incrementIteration(filename, i);
            }
            temp = new File(filename);
            i++;
        }
        return filename;
    }

    private static String incrementIteration(String oldFilename) {
        int extensionStartsAt = oldFilename.lastIndexOf('.');
        String extension = oldFilename.substring(extensionStartsAt);
        return oldFilename.substring(0, extensionStartsAt) + "(1)" + extension;
    }

    private static String incrementIteration(String oldFilename, int i) {
        int iterationStartsAt = oldFilename.lastIndexOf('(')+1;
        int iterationEndsAt = oldFilename.lastIndexOf(')');
        String filename = oldFilename.substring(0, iterationStartsAt);
        if (Integer.parseInt(oldFilename.substring(iterationStartsAt, iterationEndsAt))+1 == i) {
            filename += i;
        } else {
            filename += oldFilename.substring(iterationStartsAt, iterationEndsAt);
        }
        filename += oldFilename.substring(iterationEndsAt);
        return filename;
    }
}
