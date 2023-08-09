package game;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.example.Question;
import org.fit.pdfdom.PDFDomTree;
import org.fit.pdfdom.PDFDomTreeConfig;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

//idea to convert pdf file to html, then extract formatting data from there
public class ParsePdfToDB {
    MongoTemplate mongo;
    public ParsePdfToDB(MongoTemplate mongo) {
        this.mongo = mongo;
        //run();
    }

    private void run() {
        Question q = new Question("This animal names a religious doctrine which holds that Israel should be governed by the Haredi. Samuel claimed that one of these animals belonging to the Messiah has a hundred shades of color. Mohammed rode one of these animals named Ya’fūr which had the power of (*) speech, which was similarly granted to one of these animals owned by Balaam. Zachariah prophesied that the Messiah would ride into Jerusalem on one of these animals taken from the Mount of Olives. Samson slew a thousand Philistines with the jawbone of one of these animals. For 10 points, name this non-horse equine animal ridden by Mary and Joseph on their way to Bethlehem.", List.of("donkeys,ass,Equus africanus asinus".split(",")),"ANSWER: donkeys [accept ass or Equus africanus asinus, prompt on Equus africanus]", List.of(new QuestionTags[]{QuestionTags.HISTORY,QuestionTags.HS}));
        mongo.save(q);
        mongo.save(new Question("The penultimate section of this work begins with a G E-flat A low B motif played by trumpets and trombones before a scherzo in 6/8 time. The fifth section of this work begins with alternating half-diminished seventh chords one step apart played by the flutes and harps. The final movement of this piece features a “fade-out” featuring an (*) offstage women's chorus and both its first and last movements are in 5/4 time. The melody for the hymn “I Vow To Thee, My Country” is taken from the fourth movement of this work subtitled “The Bringer of Jollity.” For 10 points, name this suite by Gustav Holst containing such movements as “Jupiter” and “Mars.", List.of("Planets".split(",")), "the planets", List.of(new QuestionTags[]{QuestionTags.MUSIC,QuestionTags.HS})));
    }

    private void pdfdomthing() throws IOException, ParserConfigurationException {

        PDDocument pdf = PDDocument.load(new java.io.File("C:\\Users\\fishg\\IdeaProjects\\HBQB_Discord_Bot\\src\\main\\pdf\\PB Packet 12.pdf"));


        PDFDomTree parser = new PDFDomTree();

        Document dom = parser.createDOM(pdf);


    }
}
