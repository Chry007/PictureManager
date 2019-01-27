package com.arz.picmanager.service;

import com.arz.picmanager.data.ImageFile;
import com.arz.picmanager.gui.InputManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PictureFinder {

    private static final List<String> SEARCH_FILE_EXTENSIONS = Arrays.asList("JPG", "JPEG");
    private InputManager inputManager;


    public PictureFinder(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public String doWork(File searchRoot, File destination, boolean validateOutputFolder) {
        List<ImageFile> images = this.findPicturesBelow(searchRoot, destination);
        List<ImageFile> existing = new ArrayList<>();
        if (validateOutputFolder) {
            existing = this.findPicturesBelow(destination, null);
        }
        List<ImageFile> distinct = this.getDistinct(images, existing);
        this.saveDistinct(distinct, searchRoot, destination);
        int anzahlInput = images.size();
        int anzahlOutput = distinct.size();
        int anzahlDoppelt = anzahlInput - anzahlOutput;
//        this.inputManager.finish();
        return "Es wurden " + anzahlInput + " Bilder unter dem Verzeichnis '" + searchRoot.getAbsolutePath() + "' gefunden. \n" +
                "Es wurden " + anzahlOutput + " Bilder im Verzeichnis '" + destination.getAbsolutePath() + "' abgelegt. \n" +
                anzahlDoppelt + " Bilder wurden aufgrund von Dopplung nicht kopiert.";
    }

    public List<ImageFile> findPicturesBelow(File file, File destination) {
        if (file.listFiles() != null && (destination == null || !file.getAbsolutePath().startsWith(destination.getAbsolutePath()))) {
            List<File> files = Arrays.asList(file.listFiles());

            List<ImageFile> imageFiles =
                    files.stream().filter(currentFile -> currentFile.isFile() && !currentFile.isHidden() && SEARCH_FILE_EXTENSIONS.stream().anyMatch(extension -> extension.equalsIgnoreCase(currentFile.getName().substring(currentFile.getName().lastIndexOf(".") + 1))))
                            .map(currentFile -> {
                                ImageFile imageFile = new ImageFile(currentFile);
                                this.inputManager.addPicFound();
                                return imageFile;
                            }).collect(Collectors.toList());

            files.stream().filter(currentFile -> currentFile.isDirectory() && !currentFile.isHidden()).map(child -> this.findPicturesBelow(child, destination)).forEach(images -> imageFiles.addAll(images));
            return imageFiles;
        }
        return new ArrayList<ImageFile>();
    }

    public List<ImageFile> getDistinct(List<ImageFile> listContainingDuplicates, List<ImageFile> existing) {
        List<ImageFile> distinctList = new ArrayList<>();
        listContainingDuplicates.stream().forEach(current -> {
            boolean distinct = false;
            if (!distinctList.contains(current) && !existing.contains(current)) {
                distinctList.add(current);
                distinct = true;
            }
            inputManager.addPicEvaluated(distinct);
        });
        return distinctList;
    }

    private void saveDistinct(List<ImageFile> images, File root, File destination) {
        images.stream().forEach(imageFile -> {
            String rootString = root.getParent() != null ? root.getParent() : root.getAbsolutePath();
            String relativePath = imageFile.getFile().getAbsolutePath().replace(rootString, "");
            File destinationFile = new File(destination.getAbsolutePath() + "\\" + relativePath);
            File destinationFolder = new File(destinationFile.getParent());
            destinationFolder.mkdirs();
            try {
                CopyOption[] options = new CopyOption[]{StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING};
                if (destinationFile.exists()) {
                    String filename = destinationFile.getName();
                    final String extension = filename.substring(filename.lastIndexOf("."));
                    int number = 0;
                    filename = filename.substring(0, filename.lastIndexOf("."));
                    for (File currentFile : destinationFolder.listFiles()) {
                        String substring = currentFile.getName().replace(filename, "");
                        if (substring.matches("\\(\\d+\\)" + extension)) {
                            int currentNumber = Integer.parseInt(substring.substring(1, 2));
                            if (currentNumber > number) {
                                number = currentNumber;
                            }
                        }
                    }

//                    if () { //filename.endsWith("(\\d+)")) {
//                        filename = filename.substring(0, filename.lastIndexOf("(") - 1);
//                        number = Integer.parseInt(filename.substring(filename.lastIndexOf("(")).substring(1, filename.length() - 1));
//                    }
                    filename = filename + "(" + (number + 1) + ")" + extension;
                    destinationFile = new File(destinationFolder + "\\" + filename);

//                    int dialogResult = JOptionPane.showConfirmDialog(null, "Do you want to replace '" + destinationFile.getAbsolutePath() + "' with '" + imageFile + "'?", "Warning", JOptionPane.YES_NO_OPTION);
//                    if (dialogResult == JOptionPane.NO_OPTION) {
//                        save = false;
//                    }
                }

                Files.copy(imageFile.getFile().toPath(), destinationFile.toPath(), options);
                this.inputManager.addPiccopied();

            } catch (FileAlreadyExistsException e) {
                if (new File(e.getFile()).isDirectory()) {
                    System.out.println("fuck");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
    }
}
