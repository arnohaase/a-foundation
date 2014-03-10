package com.ajjpj.abase.io;

import com.ajjpj.abase.function.AFunction1NoThrow;
import com.ajjpj.abase.function.AStatement1NoThrow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;


/**
 * @author arno
 */
public class AFileTest {
    private static final File DIR = new File("testfiledir");
    private List<File> files = new ArrayList<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setup() {
        DIR.mkdir();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() {
        for(File f: files) {
            f.delete();
        }
        DIR.delete();
    }

    @Test
    public void testSimple() throws IOException {
        doTest(Charset.defaultCharset());
    }

    @Test
    public void testEncoding() throws IOException {
        doTest(Charset.forName("utf-8"));
        doTest(Charset.forName("iso-8859-1"));
    }

    private void doTest(Charset encoding) throws IOException {
        mkFile("a.txt", encoding, "a\rb\nc\r\nd\täöü");

        final AFile af = new AFile(file("a.txt"), encoding);
        assertEquals(file("a.txt"), af.getFile());
        assertEquals(Arrays.asList("a", "b", "c", "d\täöü"), af.lines());

        final List<String> parsed = new ArrayList<>();
        af.iterate(new AStatement1NoThrow<Iterator<String>>() {
            @Override public void apply(Iterator<String> param) {
                while(param.hasNext()) {
                    parsed.add(param.next());
                }
            }
        });
        assertEquals(Arrays.asList("a", "b", "c", "d\täöü"), parsed);

        final List<String> parsed2 = af.iterate(new AFunction1NoThrow<List<String>, Iterator<String>>() {
            @Override public List<String> apply(Iterator<String> param) {
                final List<String> result = new ArrayList<>();
                while(param.hasNext()) {
                    result.add(param.next());
                }
                return result;
            }
        });
        assertEquals(Arrays.asList("a", "b", "c", "d\täöü"), parsed2);
    }


    private File file(String filename) {
        return new File(DIR, filename);
    }
    private void mkFile(String filename, Charset encoding, String content) throws IOException {
        final File f = file(filename);
        files.add(f);

        final Writer w = new OutputStreamWriter(new FileOutputStream(f), encoding);
        w.write(content);
        w.close();
    }
}
