function showLoading() {
	$('body').prepend('<div class="loading"><i class="fas fa-spinner fa-spin fa-6x"></i></div>');
}
function hideLoading() {
	$('div.loading').detach();
}

     function dataURItoBlob(dataURI) {
        // convert base64/URLEncoded data component to raw binary data held in a string
        var byteString;
        if (dataURI.split(',')[0].indexOf('base64') >= 0)
            byteString = atob(dataURI.split(',')[1]);
        else
            byteString = unescape(dataURI.split(',')[1]);
        // separate out the mime component
        var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0];
        // write the bytes of the string to a typed array
        var ia = new Uint8Array(byteString.length);
        for (var i = 0; i < byteString.length; i++) {
            ia[i] = byteString.charCodeAt(i);
        }
        return new Blob([ia], {type:mimeString});
     }
     function convertToJPGBlob(imgSrc){
        if( imgSrc.name.indexOf(".pdf")!=-1 ){
            return imgSrc;
        }
        console.log("convertToJPG", imgSrc);
         return new Promise((resolve, reject) => {
            var canvas = document.createElementNS('http://www.w3.org/1999/xhtml', 'canvas');
            var ctx = canvas.getContext("2d");
            var fr = new FileReader();
            fr.onload = function(){ //load file as image
                var img = new Image;
                img.onload = function(){ // load image to canvas
                  canvas.width = img.width;
                  canvas.height = img.height;
                  ctx.drawImage(img,0,0);
                  var blob = dataURItoBlob(canvas.toDataURL('image/jpeg', 1.0));
                  blob.name = imgSrc.name;
                  resolve( blob ); // return canvas as JPG blob
                }
                img.src = fr.result;
            }
            fr.readAsDataURL(imgSrc);
         });
     }