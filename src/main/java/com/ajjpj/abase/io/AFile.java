package com.ajjpj.abase.io;

import com.ajjpj.abase.function.AFunction0;
import com.ajjpj.abase.function.AFunction1;
import com.ajjpj.abase.function.AStatement1;
import com.ajjpj.abase.util.AUnchecker;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author arno
 */
public class AFile {
    private final File file;
    private final Charset encoding;

    public AFile(String file, Charset encoding) {
        this(new File(file), encoding);
    }

    public AFile(File file, Charset encoding) {
        this.file = file;
        this.encoding = encoding;
    }

    public File getFile() {
        return file;
    }

    public List<String> lines() throws IOException {
        final List<String> result = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            String line;
            while ((line = r.readLine()) != null) {
                result.add(line);
            }
        }
        return result;
    }

    public <E extends Exception> void iterate(AStatement1<Iterator<String>, E> callback) throws E, IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            final Iterator<String> iter = new Iterator<String>() {
                private String line = r.readLine();

                @Override
                public boolean hasNext() {
                    return line != null;
                }

                @Override
                public String next() {
                    return AUnchecker.executeUnchecked(new AFunction0<String, IOException>() {
                        @Override
                        public String apply() throws IOException {
                            final String result = line;
                            line = r.readLine();
                            return result;
                        }
                    });
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

            callback.apply(iter);
        }
    }
    public <R, E extends Exception> R iterate(AFunction1<R, Iterator<String>, E> callback) throws E, IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            final Iterator<String> iter = new Iterator<String>() {
                private String line = r.readLine();

                @Override
                public boolean hasNext() {
                    return line != null;
                }

                @Override
                public String next() {
                    return AUnchecker.executeUnchecked(new AFunction0<String, IOException>() {
                        @Override
                        public String apply() throws IOException {
                            final String result = line;
                            line = r.readLine();
                            return result;
                        }
                    });
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };

            return callback.apply(iter);
        }
    }
}
