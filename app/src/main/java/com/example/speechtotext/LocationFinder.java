package com.example.speechtotext;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

//import static com.example.speechtotext.ServiceClass.a;

public class LocationFinder {
    public String a;
    private String paragraph;
    Context myContext;
    LocationFinder(String paragraph,Context context){
        this.paragraph=paragraph;
        this.myContext=context;
    }
    public void main(String args[]) throws Exception{
//        String paragraph = "Tutorialspoint is located in Hyderabad";


        AssetManager mngr = myContext.getAssets();
        InputStream is = mngr.open("en-token.bin");
        InputStream inputStreamTokenizer = mngr.open("en-token.bin");
        TokenizerModel tokenModel = new TokenizerModel(inputStreamTokenizer);

        //String paragraph = "Mike and Smith are classmates"; 
        

        //Instantiating the TokenizerME class 
        TokenizerME tokenizer = new TokenizerME(tokenModel);
        String tokens[] = tokenizer.tokenize(paragraph);

        //Loading the NER-location moodel 
        InputStream inputStreamNameFinder = mngr.open("en-ner-location.bin");
        TokenNameFinderModel model = new TokenNameFinderModel(inputStreamNameFinder);

        //Instantiating the NameFinderME class 
        NameFinderME nameFinder = new NameFinderME(model);

        //Finding the names of a location 
        Span nameSpans[] = nameFinder.find(tokens);
        //Printing the spans of the locations in the sentence 
        for(Span s: nameSpans)
            a=tokens[s.getStart()];
    }



}
