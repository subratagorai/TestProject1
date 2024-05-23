package org.pdfoperation.main;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OptimizePDF {
    private static final Logger LOGGER = Logger.getLogger(OptimizePDF.class.getName());
    private static final String BASE_DIR = "/Users/subratag/Documents/git/github/pdfoperations/src/main/resources/";
    private static final String INPUT_DIR = BASE_DIR + "input";
    private static final String INPUT_FILE="GrayScaled_ConsolidatedBill_Sample.pdf";
    //private static final String OUTPUT_DIR = BASE_DIR + "output";
    private static final String OUT_PUT_FILE = "Optimized_"+INPUT_FILE;

    private static final String TARGET_OUTPUT_DIR = "/Users/subratag/Documents/git/github/pdfoperations/target/classes/output" ;
    public static void main(String[] args) throws IOException {
        File file = new File(INPUT_DIR + "/" + INPUT_FILE);
        PDDocument document = Loader.loadPDF(file);
        OptimizePDF optimizePDF = new OptimizePDF();
        optimizePDF.optimizeMergedPdf(document);
        document.close();
    }



    static COSBase resolve(COSBase object) {
        while (object instanceof COSObject o) {
            object = o.getObject();
        }

        return object;
    }


    public void optimizeMergedPdf(PDDocument targetPDF) throws IOException {
        ByteArrayOutputStream targetPDFOutputStream = new ByteArrayOutputStream();
        targetPDF.save(targetPDFOutputStream);
        LOGGER.log(Level.INFO,"Starting optimization of merged PDF,before optimization size of merged PDF: {0} " , targetPDFOutputStream.size());

        ByteArrayOutputStream optimizedPDFOutputStream = new ByteArrayOutputStream();

        try {
            PDDocument optimizedPdfDocument = optimizeMerge(targetPDF);
            optimizedPdfDocument.save(optimizedPDFOutputStream);
            optimizedPdfDocument.save(TARGET_OUTPUT_DIR+ "/" + OUT_PUT_FILE);
            optimizedPdfDocument.close();
            LOGGER.log(Level.INFO,"Merged pdf size after optimization is: {0}", optimizedPDFOutputStream.size());
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE,"Problem encountered while optimizing merged PDF: {0}", exception.getMessage());
            throw exception;
        }

    }

    public PDDocument optimizeMerge(PDDocument pdDocument) throws IOException {
        Map<COSBase, Collection<Reference>> complexObjects = findContainers(pdDocument);

        for (int pass = 0; ; pass++) {
            int merges = mergeDuplicates(complexObjects);

            if (merges <= 0) {
                LOGGER.log(Level.INFO,"Pass {0} - No merged objects", pass);
                break;
            }
            Object[] params= {pass, merges};
            LOGGER.log(Level.INFO,"Pass {0} - Merged objects: {1} ", params);
        }

        return pdDocument;
    }

    private Map<COSBase, Collection<Reference>> findContainers(PDDocument pdDocument) {
        COSDictionary catalogDictionary = pdDocument.getDocumentCatalog().getCOSObject();
        Map<COSBase, Collection<Reference>> parentContainerElementsByChildContainer = new HashMap<>();
        parentContainerElementsByChildContainer.put(catalogDictionary, new ArrayList<>());

        Set<COSBase> parentContainers = Collections.singleton(catalogDictionary);
        Set<COSBase> childContainers = new HashSet<>();

        while (!parentContainers.isEmpty()) {
            for (COSBase parentContainer : parentContainers) {
                if (parentContainer instanceof COSArray array) {
                    for (int i = 0; i < array.size(); i++) {
                        addPotentialChildContainer(new ArrayReference(array, i), parentContainerElementsByChildContainer, childContainers);
                    }
                } else if (parentContainer instanceof COSDictionary dictionary) {
                    for (COSName key : dictionary.keySet()) {
                        addPotentialChildContainer(new DictionaryReference(dictionary, key), parentContainerElementsByChildContainer, childContainers);
                    }
                }
            }

            parentContainers = childContainers;
            childContainers = new HashSet<>();
        }
        return parentContainerElementsByChildContainer;
    }

    private void addPotentialChildContainer(
            Reference parentContainerElement,
            Map<COSBase, Collection<Reference>> parentContainerElementsByChildContainer,
            Set<COSBase> childContainers) {

        final COSBase parentContainerElementContent = parentContainerElement.getTo();

        if (isContainer(parentContainerElementContent)) {
            addChildContainer(
                    parentContainerElement,
                    parentContainerElementContent,
                    parentContainerElementsByChildContainer,
                    childContainers);
        }
    }

    private void addChildContainer(
            final Reference parentContainerElement,
            final COSBase childContainer,
            final Map<COSBase, Collection<Reference>> parentContainerElementsByChildContainer,
            final Set<COSBase> childContainers) {

        if (!parentContainerElementsByChildContainer.containsKey(childContainer)) {
            childContainers.add(childContainer);
        }

        Collection<Reference> parentContainerElements = parentContainerElementsByChildContainer
                .computeIfAbsent(childContainer, aChildContainer -> new ArrayList<>());

        parentContainerElements.add(parentContainerElement);
    }

    private boolean isContainer(final COSBase object) {
        return object instanceof COSArray || object instanceof COSDictionary;
    }

    int mergeDuplicates(Map<COSBase, Collection<Reference>> complexObjects) throws IOException {
        List<HashOfCOSBase> hashes = new ArrayList<>(complexObjects.size());
        for (COSBase object : complexObjects.keySet()) {
            hashes.add(new HashOfCOSBase(object));
        }
        Collections.sort(hashes);

        int removedDuplicates = 0;
        if (!hashes.isEmpty()) {
            int runStart = 0;
            int runHash = hashes.get(0).hash;
            for (int i = 1; i < hashes.size(); i++) {
                int hash = hashes.get(i).hash;
                if (hash != runHash) {
                    int runSize = i - runStart;
                    if (runSize != 1) {
                        Object[] params = {runHash, runSize};
                        LOGGER.log(Level.INFO,"Equal hash {0} for {1} elements.", params);
                        removedDuplicates += mergeRun(complexObjects, hashes.subList(runStart, i));
                    }
                    runHash = hash;
                    runStart = i;
                }
            }
            int runSize = hashes.size() - runStart;
            if (runSize != 1) {
                Object[] params = {runHash, runSize};
                LOGGER.log(Level.INFO,"Equal hash {0} for {1} elements.", params);
                removedDuplicates += mergeRun(complexObjects, hashes.subList(runStart, hashes.size()));
            }
        }
        return removedDuplicates;
    }

    int mergeRun(Map<COSBase, Collection<Reference>> complexObjects, List<HashOfCOSBase> run) {
        int removedDuplicates = 0;

        List<List<COSBase>> duplicateSets = new ArrayList<>();
        for (HashOfCOSBase entry : run) {
            COSBase element = entry.object;
            for (List<COSBase> duplicateSet : duplicateSets) {
                if (equals(element, duplicateSet.get(0))) {
                    duplicateSet.add(element);
                    element = null;
                    break;
                }
            }
            if (element != null) {
                List<COSBase> duplicateSet = new ArrayList<>();
                duplicateSet.add(element);
                duplicateSets.add(duplicateSet);
            }
        }

        LOGGER.log(Level.INFO,"Identified {0} sets of identical objects in run ", duplicateSets.size());

        for (List<COSBase> duplicateSet : duplicateSets) {
            if (duplicateSet.size() > 1) {
                COSBase survivor = duplicateSet.remove(0);
                Collection<Reference> survivorReferences = complexObjects.get(survivor);
                for (COSBase object : duplicateSet) {
                    Collection<Reference> references = complexObjects.get(object);
                    for (Reference reference : references) {
                        reference.setTo(survivor);
                        survivorReferences.add(reference);
                    }
                    complexObjects.remove(object);
                    removedDuplicates++;
                }
                survivor.setDirect(false);
            }
        }

        return removedDuplicates;
    }

    boolean equals(COSBase a, COSBase b) {
        if (a instanceof COSArray aArray && b instanceof COSArray bArray) {
            if (aArray.size() == bArray.size()) {
                for (int i = 0; i < aArray.size(); i++) {
                    if (!resolve(aArray.get(i)).equals(resolve(bArray.get(i))))
                        return false;
                }
                return true;
            }
        } else if (a instanceof COSDictionary aDict && b instanceof COSDictionary bDict) {
            Set<COSName> keys = aDict.keySet();
            // As proposed by  on gitHub, we can compare dictionary sizes
            // here instead of the dictionaries themselves as we compare the values
            // key by key in the body of the if statement.
            if (keys.size() == bDict.keySet().size()) {
                for (COSName key : keys) {
                    if (!resolve(aDict.getItem(key)).equals(bDict.getItem(key)))
                        return false;
                }
                // In case of COSStreams we should
                // also compare the stream contents here. But apparently
                // their hashes coincide well enough for the original
                // hashing equality, so let's just assume...
                return true;
            }

        }
        return false;
    }

    interface Reference {
        COSBase getFrom();

        COSBase getTo();

        void setTo(COSBase to);
    }

    static class ArrayReference implements Reference {
        final COSArray from;
        final int index;

        public ArrayReference(COSArray array, int index) {
            this.from = array;
            this.index = index;
        }

        @Override
        public COSBase getFrom() {
            return from;
        }

        @Override
        public COSBase getTo() {
            return resolve(from.get(index));
        }

        @Override
        public void setTo(COSBase to) {
            from.set(index, to);
        }
    }

    static class DictionaryReference implements Reference {
        final COSDictionary from;
        final COSName key;

        public DictionaryReference(COSDictionary dictionary, COSName key) {
            this.from = dictionary;
            this.key = key;
        }

        @Override
        public COSBase getFrom() {
            return from;
        }

        @Override
        public COSBase getTo() {
            return resolve(from.getDictionaryObject(key));
        }

        @Override
        public void setTo(COSBase to) {
            from.setItem(key, to);
        }
    }

    static class HashOfCOSBase implements Comparable<HashOfCOSBase> {
        final COSBase object;
        final int hash;

        public HashOfCOSBase(COSBase object) throws IOException {
            this.object = object;
            this.hash = calculateHash(object);
        }

        int calculateHash(COSBase object) throws IOException {
            if (object instanceof COSArray cosArray) {
                int result = 1;
                for (COSBase member : cosArray)
                    result = 31 * result + member.hashCode();
                return result;
            } else if (object instanceof COSDictionary cosDictionary) {
                int result = 3;
                for (Map.Entry<COSName, COSBase> entry : cosDictionary.entrySet())
                    result += entry.hashCode();
                if (object instanceof COSStream cosStream) {
                    try (InputStream data = cosStream.createRawInputStream()) {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        byte[] buffer = new byte[8192];
                        int bytesRead = 0;
                        while ((bytesRead = data.read(buffer)) >= 0)
                            md.update(buffer, 0, bytesRead);
                        result = 31 * result + Arrays.hashCode(md.digest());
                    } catch (NoSuchAlgorithmException e) {
                        throw new IOException(e);
                    }
                }
                return result;
            } else {
                throw new IllegalArgumentException(String.format("Unknown complex COSBase type %s", object.getClass().getName()));
            }
        }

        @Override
        public int compareTo(HashOfCOSBase o) {
            int result = Integer.compare(hash, o.hash);

            if (result == 0) {
                result = Integer.compare(hashCode(), o.hashCode());
            }

            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashOfCOSBase that = (HashOfCOSBase) o;
            return hash == that.hash && Objects.equals(object, that.object);
        }

        @Override
        public int hashCode() {
            return Objects.hash(object, hash);
        }

    }
}
