package com.craftinginterpreters.lox;

import java.io.*;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean errorFound = false, interactiveShell = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else {
            interactiveShell = (args.length != 1);
            execute(args.length == 1 ? args[0] : null);
        }
    }

    private static void execute(String filePath) throws IOException {
        BufferedReader br;
        if (interactiveShell) {
            InputStreamReader input = new InputStreamReader(System.in);
            br = new BufferedReader(input);
            System.out.print("> ");
        } else {
            br = new BufferedReader(new FileReader(filePath));
        }
        for (String line; (line = br.readLine()) != null; ) {
            run(line);
            if (errorFound && (!interactiveShell)) {
                System.exit(65);
            }
            if (hadRuntimeError && (!interactiveShell)) {
                System.exit(70);
            }
            if (interactiveShell) {
                if (errorFound) errorFound = false;
                System.out.print(">");
            }
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (errorFound) return;

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", -1, message);
    }

    static void error(int line, String where, int offset, String message) {
        report(line, where, offset, message);
    }

    private static void report(int line, String where, int offset, String message) {
        System.err.println(
                "[line " + line + "] Error" + "" + ": " + message);
        if (offset != -1) {
            System.err.println(where);
            System.err.println(" ".repeat(offset - 1) + "^ error might be here");
        }
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", -1, message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", -1, message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

}