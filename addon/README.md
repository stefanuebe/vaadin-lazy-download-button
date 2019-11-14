# Lazy Download Button
This component allows you to display a normal Vaadin button to download content from your application
without blocking the UI while preparing the content. This allows you to load huge files from your backend
and let the browser download it when it's ready.

Please also have a look at the examples or the demo source code, if you want to see how to integrate the LDB.

## Push needed
Since the content preparation is done in a separate thread, this component needs **Push** to be activated.
Please lookup the official documentation of Vaadin on how to activate Push: https://vaadin.com/docs/flow/advanced/tutorial-push-configuration.html

# Examples
## Create a simple LDB instance
```
// Create a new instance - it needs at least a caption (or icon) and a 
// callback to create the content on click time.
//
// Please be aware, that the callback is called OUTSIDE of the current UI.
// You need to use UI.access() if you want to access the UI.

LazyDownloadButton button = new LazyDownloadButton("Download", () -> {
    try {
        // this callback is 
        return Files.newInputStream(Paths.get("LICENSE"));
    } catch (IOException | InterruptedException e) {
        throw new RuntimeException(e);
    }
});

add(button);
```

## Create an instance with a custom file name
```
// You may also provide a callback to provide a file name on click time

LazyDownloadButton button = new LazyDownloadButton("Download", 
    () -> "my_file.txt", 
    () -> // ... create the input stream here 
);

add(button);
```

## Adding listeners / UI feedback
```
// LDB extends Vaadin Button, so you may use a simple click listener

LazyDownloadButton button = new LazyDownloadButton(...);

button.setDisableOnClick(true);
button.addClickListener(event -> {
    // show some feedback to the user, that the download is prepared in the background
    event.getSource().setText("Preparing download...");
});

// LDB also provides a "download start" listener - this listener is fired, when the client
// side starts the download. Please be aware to NOT remove the button here or its content, otherwise
// the download can fail.

button.addDownloadStartsListener(event -> {
    // restore normal state, so that the user can download the content again
    LazyDownloadButton button = event.getSource();
    button.setText("Download"); 
    button.setEnabled(true);
});
```
