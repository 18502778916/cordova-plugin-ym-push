var exec=require('cordova/exec');
module.exports={
ympush:function(content,success){
exec(success,null,"ympush","ympush",[content]);
}
};