package org.vaadin.stefan;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Extension of anchor, that will display a vaadin button as clickable instance to initiate a download. The download
 * content is generated at click time.
 */

public class LazyDownloadButton extends Button {

    private static final String DEFAULT_FILE_NAME = "download";
    private static final Supplier<String> DEFAULT_FILE_NAME_SUPPLIER = () -> DEFAULT_FILE_NAME;
    private Supplier<String> fileNameCallback;
    private InputStreamFactory inputStreamCallback;

    private Anchor anchor;

    public LazyDownloadButton() {
    }

    public LazyDownloadButton(String text) {
        super(text);
    }

    public LazyDownloadButton(Component icon) {
        super(icon);
    }

    public LazyDownloadButton(String text, InputStreamFactory inputStreamFactory) {
        this(text, DEFAULT_FILE_NAME_SUPPLIER, inputStreamFactory);
    }

    public LazyDownloadButton(Component icon, InputStreamFactory inputStreamFactory) {
        this(icon, DEFAULT_FILE_NAME_SUPPLIER, inputStreamFactory);
    }

    public LazyDownloadButton(String text, Component icon, InputStreamFactory inputStreamFactory) {
        this(text, icon, DEFAULT_FILE_NAME_SUPPLIER, inputStreamFactory);
    }

    public LazyDownloadButton(String text, Supplier<String> fileNameCallback, InputStreamFactory inputStreamFactory) {
        this(text, null, fileNameCallback, inputStreamFactory);
    }

    public LazyDownloadButton(Component icon, Supplier<String> fileNameCallback, InputStreamFactory inputStreamFactory) {
        this("", icon, fileNameCallback, inputStreamFactory);
    }

    /**
     * Creates a download button. The first two parameters are used for the button displayment.
     * <p/>
     * The third callback is called, when the download button is clicked. This is called inside the UI thread before
     * the input stream generation starts. It can be used for instance to deactivate the button.
     * <p/>
     * The fourth parameter is a callback, that is used to generate the download file name
     * <p/>
     * The fifth parameter is a callback to generate the input stream sent to the client. This callback will be
     * called in a separate thread (so that the UI thread is not blocked).
     * <p/>
     * The sixth callback is called when the download anchor on the client side has been clicked (means the input
     * stream content is now sent to the user). This callback is called inside the UI thread.
     * BE AWARE THAT THE FILE IS STILL SENT TO THE CLIENT AT THIS POINT. Deleting or removing things can interrupt the
     * download.
     *
     * @param text                button text
     * @param icon                button icon
     * @param fileNameCallback    callback for file name generation
     * @param inputStreamCallback callback for input stream generation
     */
    public LazyDownloadButton(String text, Component icon, Supplier<String> fileNameCallback, InputStreamFactory inputStreamCallback) {
        super(text);

        this.fileNameCallback = fileNameCallback;
        this.inputStreamCallback = inputStreamCallback;

        if (icon != null) {
            setIcon(icon);
        }

        // needed?
//        if (downloadStartedListener != null) {
//            getElement().addEventListener("click", event1 -> downloadStartedListener.accept(this, event1));
//        }

        /*button.*/
        super.addClickListener(event -> {
            // we add the anchor to download in the parent of the button - if there are scenarios where the anchor
            // should be placed somewhere else, this needs to be extended. Cannot be placed inside of the button
            // since the button might be disabled or invisible thus makes the anchor not usable.
            // The anchor must not be removed by this component, since the download failes otherwise
            getParent().ifPresent(component -> {
                Objects.requireNonNull(fileNameCallback, "File name callback must not be null");
                Objects.requireNonNull(inputStreamCallback, "Input stream callback must not be null");

                if (anchor == null) {
                    anchor = new Anchor();
                    Element anchorElement = anchor.getElement();
                    anchorElement.setAttribute(DEFAULT_FILE_NAME, true);
                    component.getElement().appendChild(anchor.getElement());

                    anchorElement.addEventListener("click", event1 -> fireEvent(new DownloadStartsEvent(this, true, event1)));
                }

//                if (buttonClickListener != null) {
//                    buttonClickListener.accept(this, event);
//                }

                Optional<UI> optionalUI = getUI();
                Executors.newSingleThreadExecutor().execute(() -> {
                    try {
                        InputStream inputStream = inputStreamCallback.createInputStream();

                        optionalUI.ifPresent(ui -> ui.access(() -> {
                            StreamResource href = new StreamResource(fileNameCallback.get(), () -> inputStream);
                            href.setCacheTime(0);
                            anchor.setHref(href);
                            anchor.getElement().callJsFunction("click");
                        }));

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        });
//        add(button);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (anchor != null) {
            getParent().ifPresent(component -> {
                component.getElement().removeChild(anchor.getElement());
            });
        }
    }

    public Registration addDownloadStartsListener(ComponentEventListener<DownloadStartsEvent> listener) {
        return addListener(DownloadStartsEvent.class, listener);
    }

    public Supplier<String> getFileNameCallback() {
        return fileNameCallback;
    }

    public void setFileNameCallback(Supplier<String> fileNameCallback) {
        this.fileNameCallback = fileNameCallback;
    }

    public InputStreamFactory getInputStreamCallback() {
        return inputStreamCallback;
    }

    public void setInputStreamCallback(InputStreamFactory inputStreamCallback) {
        this.inputStreamCallback = inputStreamCallback;
    }

    public static class DownloadStartsEvent extends ComponentEvent<LazyDownloadButton> {

        private final DomEvent clientSideEvent;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source     the source component
         * @param fromClient <code>true</code> if the event originated from the client
         */
        public DownloadStartsEvent(LazyDownloadButton source, boolean fromClient, DomEvent clientSideEvent) {
            super(source, fromClient);
            this.clientSideEvent = clientSideEvent;
        }

        public DomEvent getClientSideEvent() {
            return clientSideEvent;
        }
    }
}
