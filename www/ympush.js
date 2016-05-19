var exec=require('cordova/exec');
var ym_push={
startWork:function(content,success){
exec(success,null,"YmPush","startWork",[content]);
},
setTag:function(content,success){
exec(success,null,"YmPush","setTag",[content]);
},
deleteTag:function(content,success){
exec(success,null,"YmPush","deleteTag",[content]);
}
};
module.exports = ym_push;