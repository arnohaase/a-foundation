package com.ajjpj.afoundation.io;

import com.ajjpj.afoundation.collection.ACollectionHelper;
import com.ajjpj.afoundation.collection.immutable.AOption;
import com.ajjpj.afoundation.collection.immutable.ATraversable;
import com.ajjpj.afoundation.function.*;
import com.ajjpj.afoundation.util.AUnchecker;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author arno
 */
public class AFile implements ATraversable<String> {
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
                    return AUnchecker.executeUnchecked(new AFunction0<String, IOException> () {
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

    public <R, E extends Exception> R iterate(AFunction1<? super Iterator<String>, ? extends R, E> callback) throws E, IOException {
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




    private Iterator<String> iterator(final BufferedReader r) throws IOException {
        return new Iterator<String>() {
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
    }

    @Override public <E extends Exception> void forEach(AStatement1<? super String, E> f) throws E {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            final Iterator<String> iter = iterator(r);
            while(iter.hasNext()) {
                f.apply(iter.next());
            }
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
        }
    }

    @Override
    public <E extends Exception> ATraversable<String> filter(APredicate<? super String, E> pred) throws E {
        try {
            return ACollectionHelper.asACollectionView(ACollectionHelper.filter(lines(), pred));
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    @Override
    public <X, E extends Exception> ATraversable<X> map(AFunction1<? super String, ? extends X, E> f) throws E {
        try {
            return ACollectionHelper.asACollectionView(ACollectionHelper.map(lines(), f));
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    @Override
    public <X, E extends Exception> ATraversable<X> flatMap(AFunction1<? super String, ? extends Iterable<X>, E> f) throws E {
        try {
            return ACollectionHelper.asACollectionView(ACollectionHelper.flatMap(lines(), f));
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    @Override
    public <X, E extends Exception> ATraversable<X> collect (APartialFunction<? super String, ? extends X, E> pf) throws E {
        try {
            return ACollectionHelper.asACollectionView (ACollectionHelper.collect (lines(), pf));
        } catch (IOException e) {
            AUnchecker.throwUnchecked (e);
            return null; // for the compiler
        }
    }

    @Override public <R, E extends Exception> R foldLeft (R startValue, AFunction2<R, ? super String, R, E> f) throws E {
        try {
            return ACollectionHelper.foldLeft (lines(), startValue, f);
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
            return null; // for the compiler
        }
    }

    @Override public <E extends Exception> AOption<String> find(APredicate<? super String, E> pred) throws E {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            final Iterator<String> iter = iterator(r);
            while(iter.hasNext()) {
                final String candidate = iter.next();
                if(pred.apply(candidate)) {
                    return AOption.some(candidate);
                }
            }
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
        }
        return AOption.none();
    }

    @Override public <X> ATraversable<X> flatten() {
        throw new UnsupportedOperationException();
    }

    @Override public <E extends Exception> boolean forAll(APredicate<? super String, E> pred) throws E {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            final Iterator<String> iter = iterator(r);
            while(iter.hasNext()) {
                if(! pred.apply(iter.next())) {
                    return false;
                }
            }
        } catch (IOException e) {
            AUnchecker.throwUnchecked(e);
        }
        return true;
    }

    @Override public <E extends Exception> boolean exists(APredicate<? super String, E> pred) throws E {
        return find(pred).isDefined();
    }
}
