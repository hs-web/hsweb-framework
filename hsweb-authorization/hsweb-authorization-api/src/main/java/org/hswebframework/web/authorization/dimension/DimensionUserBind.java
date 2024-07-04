package org.hswebframework.web.authorization.dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DimensionUserBind implements Externalizable {
    private static final long serialVersionUID = -6849794470754667710L;

    private String userId;

    private String dimensionType;

    private String dimensionId;

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(userId);
        out.writeUTF(dimensionType);
        out.writeUTF(dimensionId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        userId = in.readUTF();
        dimensionType = in.readUTF();
        dimensionId = in.readUTF();
    }
}
