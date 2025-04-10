from model import train_model
from read_data import read_data,remove_duplicates
from missing_scale import fill_missing_values,scale_data
from transportation_predict_syste_demo.transportation_python.data_analysis import data_analysis

#导入文件
file_path_train = './data/train_10000.csv'
file_path_val = './data/validate_1000.csv'

df,y = read_data(file_path_train)
df_val,y_val = read_data(file_path_val)

#特征分析
data_analysis(df,y)

#删除重复值
df,y = remove_duplicates(df,y)

#填充缺失值
df = fill_missing_values(df)
df_val = fill_missing_values(df_val)

#标准化数据集
df = scale_data(df)
df_val  = scale_data(df_val)

#模型训练
model_name = train_model(df,y,df_val,y_val)