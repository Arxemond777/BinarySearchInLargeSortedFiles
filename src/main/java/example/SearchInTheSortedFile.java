package example;

import example.util.Pair;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class SearchInTheSortedFile {
    private static final Map<String, Long> CACHE = new LinkedHashMap<>();
    private static final int MAX_CACHE_KEY_SIZE = 2;
    private static final int MAX_LENGTH_OF_TWO_AND_HALF_STRINGS = (int)(16.0f * 3f); // max length of the longest string X 3
    private static final String STRING_SEPARATOR = ";";

    public static void main(String[] args) throws Exception {
        System.out.println("Start of initialization the cache: " + LocalDateTime.now());
        final long start = System.nanoTime();
        initCache(FillFiller.FILE);
        System.out.println("End of initialization the cache: " + LocalDateTime.now());
        System.out.println("Elapsed of initialization the cache:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " Msec");

        getFromSource();
    }

    private static void getFromSource() throws Exception {
        System.out.println("Input val for search");
        BufferedReader bf = null;
        try  { // BufferedReader won`t be in the CLOSEABLE function
            bf = new BufferedReader(new InputStreamReader(System.in));
            final String word = bf.readLine();

            if (word.isEmpty()) {
                System.out.println("Empty val has been entered, please try again");
                getFromSource();
            }

            System.out.println("Input: \"" + word + "\"");

            System.out.println("Start search: " + LocalDateTime.now());

            final long start = System.nanoTime();
            final String s = binarySearch(word, findRange(word), new AtomicInteger());
            System.out.println("FOUND: " + s);
            System.out.println("End search: " + LocalDateTime.now());
            System.out.println("Elapsed:" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + " Msec");
        } catch (Exception e) {
            if (bf != null)
                bf.close();
        }

        getFromSource();
    }

    private static String binarySearch(final String requiredWord, final Pair<Long, Long> rangeResult, AtomicInteger i) throws IOException, InterruptedException {
        if (rangeResult == null) {
            return null;
        }

        final Long
                start = rangeResult.getKey(),
                end = rangeResult.getValue(),
                N = end - start;


        /**
         * bytesSum = (end - start - 1)
         *
         * right = byte / 2 (with shift to the right)
         * isRight = right.split({@link System#lineSeparator()})
         *
         * if (isRight.length > 1)
         *   isRight[1]; // for exclude str subst
         *
         *
         */
        /*try (final RandomAccessFile rad = new RandomAccessFile(path, "r")) {
            rad.seek(start);
            final byte[] bytes = new byte[Math.toIntExact(end - start)];
            rad.read(bytes);
            System.out.println("______");
            final String findInRange = new String(bytes);
            if(findInRange.contains(requiredWord)) {
                System.out.println(findInRange);
            }
            System.out.println("______");
        }*/

        /*try (final RandomAccessFile rad = new RandomAccessFile(path, "r")) {
            rad.seek(start);
            final byte[] bytes = new byte[Math.toIntExact(N)];
            rad.read(bytes);
            System.out.println("______");
            final String findInRange = new String(bytes);
            if(findInRange.contains(requiredWord)) {
                System.out.println(findInRange);
            }
            System.out.println("______");
        }*/

        final Long NMinusHalfN  = N - (N >> 1); // N - ((N) / 2); where N end-range

        final Long left = (N % 2 == 0 ? NMinusHalfN : NMinusHalfN - 1)/*+MAX_LENGTH_OF_TWO_AND_HALF_STRINGS*/;
        final Long right = NMinusHalfN /*+ MAX_LENGTH_OF_TWO_AND_HALF_STRINGS*/;


        /**
         * X = (N / 2) -> MAX_LENGTH_OF_TWO_AND_HALF_STRINGS
         *
         *
         */
        try (final RandomAccessFile rad = new RandomAccessFile(FillFiller.FILE, "r")) {
            rad.seek(start);
            final byte[] bytes = new byte[MAX_LENGTH_OF_TWO_AND_HALF_STRINGS/*+10*/];
            rad.read(bytes);
//            System.out.println("______");
            final String findInRange = new String(bytes);

            final boolean contains = Pattern.
                    compile("^" + requiredWord + "\\" + STRING_SEPARATOR, Pattern.MULTILINE)
                    .matcher(findInRange)
                    .find();
//            Pattern.matches(pattern, findInRange);
//            final boolean contains = findInRange.contains(requiredWord);
            if (contains) {
                final int i1 = findInRange.indexOf(requiredWord);
                final String startRes = findInRange.substring(i1); // cut first unnecessary strings
                final int i2 = startRes.indexOf(System.lineSeparator());// detect end of the string

                if (i2 == -1) { // if we got a string with the crop in the middle of it
                    rad.seek(start+i1); // start + shift
                    final byte[] b = new byte[MAX_LENGTH_OF_TWO_AND_HALF_STRINGS];
                    rad.read(b);
                    final String s = new String(b);
                    final int i22 = s.indexOf(System.lineSeparator());

                    if (i22 == -1) // if the required val is LAST STRING
                        return s;

                    final String finalRes = s.substring(0, i22);
                    return finalRes;
                } else {
//                    System.out.println(startRes);

                    final String finalRes = startRes.substring(0, i2); // cut last unnecessary strings
                    return finalRes;
                }

            } else {
                final String[] split = findInRange.split(System.lineSeparator());

                if (split.length > 0 && (rangeResult.getValue() - rangeResult.getKey() > MAX_LENGTH_OF_TWO_AND_HALF_STRINGS)) {
                    final String s = split[1].split(STRING_SEPARATOR)[0];
                    final int isEqual = s.compareTo(requiredWord);

                    if (isEqual > 0) {
                        return binarySearch(requiredWord, new Pair<>(start, start+left), i); // left: start -> start + left
                    } else if (isEqual < 0) {
                        return binarySearch(requiredWord, new Pair<>(end - right, end), i); // right: end-right -> end
                    } else {
                        System.err.println("---------NOT FOUND--------------");
                    }
                } else {
//                    System.out.println(findInRange);
                    System.out.println("NOT FOUND #33: " + requiredWord);
                }
            }

            System.out.println("+++++++");
        }

        return null;
    }


    private static Pair<Long, Long> findRange(String str) {
        if (str == null || str.isEmpty()) return null;
        final String keySubst = str.substring(0, Math.min(str.length(), MAX_CACHE_KEY_SIZE));
        if (!CACHE.containsKey(keySubst)) {
            System.out.println("There is not such a value");
            return null;
        }
//            throw new RuntimeException("Not found");

        final Long start = CACHE.get(keySubst);

        final ArrayList<String> list = new ArrayList<>(CACHE.keySet()); // find next idx
        final int end = list.indexOf(keySubst) + 1;
        final Long endIdx = (Long) CACHE.values().toArray()[end];


        return new Pair<>(start, endIdx);
    }

    private static void initCache(String path) {
        long length = 1L;

        try (final BufferedReader bf = new BufferedReader(new FileReader(path), 32768)) {
            final String s = bf.readLine();// skip first
            length += s.getBytes().length; // todo shift for first

            String line;
            String prevLine = null;
            while ((line = bf.readLine()) != null) {
                if (
                        prevLine == null // init
                                || prevLine.charAt(0) != line.charAt(0)
                                || (
                                prevLine.length() > 1 // compare 2 => letter
                                        && line.length() > 1
                                        && prevLine.charAt(1) != line.charAt(1)
                                )
                ) {
                    prevLine = line;

                    CACHE.put(line.substring(0, Math.min(line.length(), MAX_CACHE_KEY_SIZE)), length);
                }

                length += (line.getBytes().length + 1); // +1 for each new string (\n)
            }

            CACHE.put("endOfFile", length - 1); // -1 exclude last new str

            System.out.println(CACHE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
