package ru.lartech.demo;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import ru.lartech.demo.parser.DoubleSpaceCounter;
import ru.lartech.demo.parser.ParseResult;
import ru.lartech.demo.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by z003cptz on 23.03.2016.
 */
public class Main {
    private static File scanFolder;
    private static File targetFile;
    private static int threadNumber;

    public static void main(String... args) throws IOException {
        if (checkArguments(args)) {
            Parser parser = new Parser(scanFolder, DoubleSpaceCounter.INSTANCE, threadNumber);
            List<ParseResult> result = parser.execute();
            if (result.size() == 0) {
                System.out.println("No files found in target directory");
                return;
            }

            List<String> content = new LinkedList<>();
            //sort desc way
            result.sort((r1, r2) -> Integer.compare(r2.getTokens().size(), r1.getTokens().size()));
            int maximumTokenSize = result.get(0).getTokens().size();

            for (ParseResult parseResult : result) {
                if (parseResult.getTokens().size() < maximumTokenSize) {
                    break;
                }
                StringBuilder builder = new StringBuilder();
                builder.append(parseResult.getIndex());
                parseResult.getTokens().forEach(token -> builder.append(" ").append(token));
                content.add(builder.toString());
            }

            FileUtils.writeLines(targetFile, content, false);
            System.out.println("done");
        }
    }

    protected static boolean checkArguments(String... args) {
        try {
            Preconditions.checkArgument(args.length == 3, "Please provide 3 arguments: <directroryPath> <outputFilePath> <threadPoolMaxPoolSize>");

            scanFolder = new File(args[0]);
            targetFile = new File(args[1]);
            threadNumber = Integer.parseInt(args[2]);

            Preconditions.checkArgument(scanFolder.exists() && scanFolder.canRead(), "Cannot access scan directory");
            Preconditions.checkArgument(threadNumber >= 3, "threadPoolMaxPoolSize must be greater or equal to 3");

            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }

            return true;
        } catch (NumberFormatException e) {
            System.out.println("threadPoolMaxPoolSize must be an integer");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("Failed to create output file");
        }

        return false;
    }
}
