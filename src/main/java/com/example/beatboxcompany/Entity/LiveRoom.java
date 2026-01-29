package com.example.beatboxcompany.Entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "live_rooms") // Dùng cái này cho MongoDB
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoom {

    @Id
    private String roomId;

    private String hostId;
    private String hostName;
    private String title;
    private String status;
    private Date startedAt;
}
