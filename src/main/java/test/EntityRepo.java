package test;

import a8i.A8i;
import a8i.annotate.Bind;
import a8i.jdbc.Persistence;

public class EntityRepo {

    @Bind
    A8i.Repo repo;

    public void saveEntity(Entity entity){
        String sql = "insert into entities (description, active) values ('[+]',[+])";
        persistence.save(sql, new Object[]{
                entity.getDescription(),
                entity.isActive()
        });
    }

}
