package uni.project.a.b.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

/*@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

 */
public class AppMessage {

  // @Id
   private Long id;

   private String body;

   private LocalDateTime time;

   //the name of the user?
   private Long senderId;



}
