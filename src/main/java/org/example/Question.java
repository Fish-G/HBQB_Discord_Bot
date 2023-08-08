package org.example;

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
    List<String> answers = new ArrayList<>();
    List<String> tags = new ArrayList<>();;

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public List<String> getTags() {
        return tags;
    }


}
