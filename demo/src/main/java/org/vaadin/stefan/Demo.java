package org.vaadin.stefan;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Push
@Route("")
@StyleSheet("global.css")
public class Demo extends VerticalLayout {

    private ProgressBar bar;

    public Demo() {
        add(new H4("Lazy download button demo, version 1.0.0"));
        add(new Span("All examples download the Apache License 2.0 as text. The download has an artificial timeout of 2.5 seconds to show the still active UI during the download."));

        add(createTitle("Basic example"));
        add(new Span("This example starts the download in the background. There is no feedback for the user so clicking multiple times can lead to errors. It shall only show the working download itself."));
        add(new LazyDownloadButton("Download", VaadinIcon.DOWNLOAD.create(), this::getFileName, this::createFileInputStream));

        add(createTitle("Examples with feedback"));
        add(new Span("These examples disable the button on click and give some visual feedback to the user while preparing the download. As soon as the download has started, the button will become active again."));

        // This example shows a spinner and special text inside the button
        LazyDownloadButton exampleWithListeners = new LazyDownloadButton("Download", VaadinIcon.DOWNLOAD.create(), this::getFileName, this::createFileInputStream);
        exampleWithListeners.setDisableOnClick(true);
        exampleWithListeners.addClickListener(this::onClick);
        exampleWithListeners.addDownloadStartsListener(this::onDownloadStarted);
        add(exampleWithListeners);

        add(createTitle("Different displayment variations"));

        HorizontalLayout examples = new HorizontalLayout();
        add(examples);

        // Icon only variation
        LazyDownloadButton variant = new LazyDownloadButton(VaadinIcon.DOWNLOAD.create(), this::getFileName, this::createFileInputStream);
        variant.setDisableOnClick(true);
        variant.addDownloadStartsListener(downloadStartsEvent -> downloadStartsEvent.getSource().setEnabled(true));
        examples.add(variant);

        // Tertiary small variation
        LazyDownloadButton variant2 = new LazyDownloadButton("Download", VaadinIcon.DOWNLOAD.create(), this::getFileName, this::createFileInputStream);
        variant2.setDisableOnClick(true);
        variant2.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        variant2.addClickListener(this::onClick);
        variant2.addDownloadStartsListener(this::onDownloadStarted);
        examples.add(variant2);

        // Primary success variation
        LazyDownloadButton variant3 = new LazyDownloadButton("Download", VaadinIcon.DOWNLOAD.create(), this::getFileName, this::createFileInputStream);
        variant3.setDisableOnClick(true);
        variant3.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        variant3.addClickListener(this::onClick);
        variant3.addDownloadStartsListener(this::onDownloadStarted);
        examples.add(variant3);

        add(createTitle("Normal download via Button (also uses an anchor)"));
        add(new Span("A way to integrate a download via Button is to wrap it with an Anchor element. Disadvantage of this approach is, that" +
                " the UI is blocked until the download starts. When providing large files, this interrupts the UI interaction."));

        StreamResource href = new StreamResource("old-" + getFileName(), this::createFileInputStream);
        href.setCacheTime(0);

        Anchor download = new Anchor(href, "");
        download.getElement().setAttribute("download", true);
        Button button = new Button("Eager download", new Icon(VaadinIcon.DOWNLOAD_ALT));
        button.setDisableOnClick(true);
        download.add(button);
        add(download);
    }

    private void onDownloadStarted(LazyDownloadButton.DownloadStartsEvent downloadStartsEvent) {
        LazyDownloadButton button = downloadStartsEvent.getSource();
        button.setIcon(VaadinIcon.DOWNLOAD.create());
        button.setText("Download");
        button.setEnabled(true);
    }


    private void onClick(ClickEvent<Button> buttonClickEvent) {
        Button button = buttonClickEvent.getSource();
        button.setText("Preparing download...");
    }

    private Component createTitle(String text) {
        return new H5(text);
    }

    private String getFileName() {
        return "license.txt";
    }

    private InputStream createFileInputStream() {
        try {
            Thread.sleep(2500);
            return Files.newInputStream(Paths.get("LICENSE"));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
