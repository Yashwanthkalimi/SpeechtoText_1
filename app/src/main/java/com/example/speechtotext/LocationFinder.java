package com.example.speechtotext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

//import static com.example.speechtotext.ServiceClass.a;

//public class LocationFinder<tokens> {
//    public String a;
//    private String paragraph;
//
//    Context myContext;
//    LocationFinder(String paragraph,Context context){
//        this.paragraph=paragraph;
//        this.myContext=context;
//    }
//
////        String paragraph = "Tutorialspoint is located in Hyderabad";
//
//
//        AssetManager mngr = myContext.getAssets();
//        InputStream is;
//
//    {
//        try {
//            is = mngr.open("en-token.bin");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    InputStream inputStreamTokenizer;
//
//    {
//        try {
//            inputStreamTokenizer = mngr.open("en-token.bin");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    TokenizerModel tokenModel;
//
//    {
//        try {
//            tokenModel = new TokenizerModel(inputStreamTokenizer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //String paragraph = "Mike and Smith are classmates";
//
//
//        //Instantiating the TokenizerME class
//        TokenizerME tokenizer = new TokenizerME(tokenModel);
//        String tokens[] = tokenizer.tokenize(paragraph);
//
//        //Loading the NER-location moodel
//        InputStream inputStreamNameFinder;
//
//    {
//        try {
//            inputStreamNameFinder = mngr.open("en-ner-location.bin");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    TokenNameFinderModel model;
//
//    {
//        try {
//            model = new TokenNameFinderModel(inputStreamNameFinder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    //Instantiating the NameFinderME class
//        NameFinderME nameFinder = new NameFinderME(model);
//
//        //Finding the names of a location
//        String b = String.valueOf(nameFinder.find(tokens));
//        //Printing the spans of the locations in the sentence
//
//    }


public class LocationFinder {
public String a;
    public String findLocation(String paragraph) throws IOException {
        InputStream inputStreamNameFinder = new
                FileInputStream("C:\\Users\\YASHWANTH REDDY\\Desktop\\SER+NAV\\app\\src\\main\\resources\\en-ner-location.bin");

//        InputStream inputStreamNameFinder = getClass().getResourceAsStream("/en-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);

        NameFinderME locFinder = new NameFinderME(model);
        String[] tokens = tokenize(paragraph);

        Span nameSpans[] = locFinder.find(tokens);
        for(Span span : nameSpans)

            a= tokens[span.getStart()];
        return a;
    }
    public String[] tokenize(String sentence) throws IOException{
        InputStream inputStreamTokenizer = new
                FileInputStream("C:\\Users\\YASHWANTH REDDY\\Desktop\\SER+NAV\\app\\src\\main\\resources\\en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);

        TokenizerME tokenizer = new TokenizerME(tokenModel);
        return tokenizer.tokenize(sentence);
    }
    public InputStream openBin(String filename) throws IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();
        File file = new File(classLoader.getResource(filename).getPath());
        InputStream input = new FileInputStream(file);
        return input;
    }


}

