package ru.otus.hw.models.mongo;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "comments")
public class MongoComment {
    @Id
    private String id;

    private String text;

    @Indexed
    private String bookId;

    @DocumentReference(lazy = true)
    @Transient
    private MongoBook book;


    public MongoComment(String text, String bookId) {
        this.text = text;
        this.bookId = bookId;
    }
}
