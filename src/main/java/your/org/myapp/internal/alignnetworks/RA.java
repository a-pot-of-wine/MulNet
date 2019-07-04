package your.org.myapp.internal.alignnetworks;
import org.ujmp.core.*;
import org.ujmp.core.calculation.Calculation;
public class RA {
/*
	 * 功能：根据资源分配理论计算相似性矩阵
	 * 输入：网络的邻接矩阵 
	 * 输出：相似性矩阵
	 */

    //adjacencyMatrix和simMatrix中的坐标代表节点的序号，从0开始
    public static Matrix RA_index(Matrix adjacencyMatrix){
        Matrix temp,simMatrix;
        long row = adjacencyMatrix.getRowCount();
        long column = adjacencyMatrix.getColumnCount();
        temp = Matrix.Factory.zeros(row,column);
        for(int i=0;i<row;i++){
            for (int j=0;j<column;j++){
                temp.setAsDouble(adjacencyMatrix.selectRows(Calculation.Ret.NEW,i).getValueSum(),i,j);
            }
        }
        temp = adjacencyMatrix.divide(temp);
        for (int i=0;i<row;i++){
            for (int j=0;j<column;j++){
                if (Double.isNaN(temp.getAsDouble(i,j))||Double.isInfinite(temp.getAsDouble(i,j))){
                    temp.setAsDouble(0,i,j);
                }
            }
        }

        simMatrix = adjacencyMatrix.mtimes(temp);
        simMatrix = simMatrix.times(adjacencyMatrix);
        double simMatrix_max = simMatrix.getMaxValue();
        double simMatrix_min = simMatrix.getMinValue();

        Matrix ones = Matrix.Factory.ones(row,column);

        ones = ones.times(simMatrix_min);
        simMatrix = simMatrix.minus(ones);
        simMatrix = simMatrix.divide(simMatrix_max-simMatrix_min);

        for(int i = 0;i<row;i++){
            for (int j=0;j<column;j++){
                if(i==j){
                   simMatrix.setAsDouble(0,i,j);
                }
            }
        }
        return simMatrix;
    }
}