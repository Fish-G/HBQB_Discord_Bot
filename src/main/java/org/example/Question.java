package org.example;

import game.QuestionTags;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Question {
    String id = ObjectId.get().toHexString();
    String question;
    String displayAnswers;
    List<String> answers;
    List<QuestionTags> tags;

    public Question(String question, List<String> answers,String displayAnswers, List<QuestionTags> tags) {
        this.question = question;
        this.answers = answers;
        this.tags = tags;
        this.displayAnswers = displayAnswers;
    }

    public String getDisplayAnswers() {
        return displayAnswers;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public List<QuestionTags> getTags() {
        return tags;
    }


}
