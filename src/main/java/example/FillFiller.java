package example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class FillFiller {

    public static final String FILE = "/tmp/file.csv";
    public static final String STRING_SEPARATOR = ";";
    public static void main(String[] args) throws IOException {
        writeCsv(FILE);
    }


    private static void writeCsv(String path) throws IOException {
        Objects.requireNonNull(path);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(path), 32768)) {


            out.write("name" + STRING_SEPARATOR + "Id" + System.lineSeparator());
            out.flush();

            StringBuilder sb = new StringBuilder(10);
            AtomicLong atomicLong = new AtomicLong(1);


            for (char c = 'a'; c <= 'z'; c++) // 26
                for (char c1 = 'a'; c1 <= 'z'; c1++)  // 676
//                for (char c2 = 'a'; c2 <= 'z'; c2++)  // 17576
//                    for (char c3 = 'a'; c3 <= 'z'; c3++)  // 456976
//                        for (char c4 = 'a'; c4 <= 'z'; c4++)  // 11881376 (160 mb)
                    for (char c5 = 'a'; c5 <= 'z'; c5++) { // 308915776 ()
                        sb
                                .append(c)
                                .append(c1)
//                                        .append(c2)
//                                        .append(c3)
//                                        .append(c4)
                                .append(c5)
                                .append(STRING_SEPARATOR)
                                .append(atomicLong.getAndIncrement())
                                .append(System.lineSeparator())
                        ;

//                                out.flush();
                        out.write(sb.toString());
                        //                out.newLine();
                        sb.setLength(0);
                    }


        }
    }
}